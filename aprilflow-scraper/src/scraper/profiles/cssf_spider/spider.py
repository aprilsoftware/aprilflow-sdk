# Copyright 2026 April Software
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

from __future__ import annotations

from pathlib import Path
import re
from typing import Iterable, Iterator
from urllib.parse import urlparse as _urlparse

import scrapy
from scrapy_playwright.page import PageMethod

from scraper.profiles.cssf_spider.common import (
    urlparse,
    classify_org,
    CsvWriters,
    save_bytes,
    ext_from_response,
    is_html_response,
    extract_file_links,
)
from scraper.profiles.cssf_spider.locations import build_handlers


DOWNLOAD_EXTS = (".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".zip")


class CssfFrameworkSpider(scrapy.Spider):
    name = "cssf-legal-framework"
    start_urls = ["https://www.cssf.lu/en/regulatory-framework/"]

    RIGHT_LABEL_RE = re.compile(r"^(PDF|Lien|Link|XLSX|DOCX?|PPTX?|ZIP)\b", re.I)
    LANG_PRIORITY = {"en": 0, "fr": 1, "de": 2, "unknown": 99}

    handle_httpstatus_all = True

    def __init__(self, out_dir: str, limit: int | None = None, **kwargs):
        super().__init__(**kwargs)
        self.out_dir = Path(out_dir)
        self.out_dir.mkdir(parents=True, exist_ok=True)

        self.limit = limit
        self.count = 0

        self.csv = CsvWriters(self.out_dir)
        self.handlers = build_handlers()

    def write_all(self, url: str, kind: str, *, source_url: str | None = None):
        self.csv.write_all(url, kind, source_url=source_url)

    def write_failed(
        self,
        attempted_url: str,
        kind: str,
        *,
        reason: str,
        source_url: str | None = None,
    ):
        self.csv.write_failed(attempted_url, kind, reason=reason, source_url=source_url)

    def _inc_and_check_limit(self) -> bool:
        if self.limit is not None and self.count >= self.limit:
            return False
        self.count += 1
        return True

    def make_download_request(
        self,
        url: str,
        *,
        kind: str = "download",
        source_url: str | None = None,
        meta_extra: dict | None = None,
    ) -> scrapy.Request:
        meta = {"kind": kind}
        if source_url:
            meta["source_url"] = source_url
        if meta_extra:
            meta.update(meta_extra)

        return scrapy.Request(
            url,
            callback=self.parse_download,
            errback=self.on_link_error,
            meta=meta,
            dont_filter=True,
        )

    def _reason_from_response(self, response: scrapy.http.Response) -> str:
        status = response.status
        host = (_urlparse(response.url).netloc or "").lower()
        ctype = response.headers.get(b"Content-Type", b"").decode("utf-8", "ignore").lower()

        if response.status == 200 and is_html_response(response):
            if "eur-lex.europa.eu" in host:
                body = (getattr(response, "text", "") or "")[:7000].lower()
                if (
                    ("javascript" in body and ("disabled" in body or "enable" in body))
                    or ("not a robot" in body)
                    or ("captcha" in body)
                    or ("verify" in body and "robot" in body)
                ):
                    return "HTML response (EUR-Lex anti-bot/JS interstitial)"
            return "HTML response (not a document)"

        if "eur-lex.europa.eu" in host and status in (202, 403):
            if "text/html" in ctype and is_html_response(response):
                body = (getattr(response, "text", "") or "")[:7000].lower()
                if (
                    ("javascript" in body and ("disabled" in body or "enable" in body))
                    or ("not a robot" in body)
                    or ("captcha" in body)
                    or ("verify" in body and "robot" in body)
                ):
                    return f"HTTP {status} (EUR-Lex anti-bot/JS interstitial)"
                return f"HTTP {status} (EUR-Lex returned HTML; likely interstitial/throttle)"
            return f"HTTP {status} (EUR-Lex did not deliver file)"

        return f"HTTP {status}"

    def _is_eurlex(self, url: str) -> bool:
        return "eur-lex.europa.eu" in ((_urlparse(url).netloc or "").lower())

    def _should_playwright_retry(self, response: scrapy.http.Response) -> bool:
        if not self._is_eurlex(response.url):
            return False

        if response.status in (202, 403):
            return True
        if response.status == 200 and is_html_response(response):
            return True

        return False

    def _playwright_retry_request(self, response: scrapy.http.Response) -> scrapy.Request | None:
        src_url = response.meta.get("source_url", response.url)
        kind = response.meta.get("kind", "download")

        if response.meta.get("pw_retry_done"):
            return None

        meta = {
            **response.meta,
            "pw_retry_done": True,
            "playwright": True,
            "playwright_page_goto_kwargs": {"wait_until": "domcontentloaded", "timeout": 90_000},
            "playwright_page_methods": [PageMethod("wait_for_timeout", 1200)],
            "source_url": src_url,
            "kind": kind,
        }

        return scrapy.Request(
            response.url,
            callback=self.parse_eurlex_playwright,
            errback=self.on_link_error,
            meta=meta,
            dont_filter=True,
        )

    def parse_eurlex_playwright(self, response: scrapy.http.Response):
        src_url = response.meta.get("source_url", response.url)

        if response.status == 200 and not is_html_response(response):
            response.meta.setdefault("source_url", src_url)
            response.meta.setdefault("kind", "download")
            yield from self.save_download_response(response, kind="download")
            return

        if response.status != 200:
            self.write_failed(
                response.url,
                response.meta.get("kind", "download"),
                source_url=src_url,
                reason=self._reason_from_response(response),
            )
            return

        if not is_html_response(response):
            self.write_failed(
                response.url,
                response.meta.get("kind", "download"),
                source_url=src_url,
                reason="Playwright retry returned non-text content (cannot extract links)",
            )
            return

        links = extract_file_links(response)

        for href in response.css("a::attr(href)").getall():
            if not href:
                continue
            u = response.urljoin(href)
            p = (_urlparse(u).path or "").lower()
            if "/txt/pdf" in p and u not in links:
                links.append(u)

        pdf_links = [
            u for u in links
            if "/txt/pdf" in (_urlparse(u).path or "").lower() or u.lower().endswith(".pdf")
        ]

        if pdf_links:
            yield self.make_download_request(pdf_links[0], kind="download", source_url=src_url)
            return

        self.write_failed(
            response.url,
            response.meta.get("kind", "download"),
            source_url=src_url,
            reason="Playwright retry: could not find PDF link in rendered page",
        )

    def save_download_response(self, response: scrapy.http.Response, *, kind: str = "download"):
        src = response.meta.get("source_url", response.url)

        if response.status == 200 and is_html_response(response):
            self.write_failed(
                response.url,
                kind,
                source_url=src,
                reason=self._reason_from_response(response),
            )
            return

        if response.status != 200:
            self.write_failed(
                response.url,
                kind,
                source_url=src,
                reason=self._reason_from_response(response),
            )
            return

        if not self._inc_and_check_limit():
            return

        ext = ext_from_response(response)
        fp = save_bytes(self.out_dir, seq=self.count, url=response.url, body=response.body, ext=ext)
        yield {"saved_path": str(fp)}

    def closed(self, reason):
        self.csv.close()

    def on_link_error(self, failure):
        req = failure.request
        kind = req.meta.get("kind", "download")
        src_url = req.meta.get("source_url", req.url)

        err_msg = failure.getErrorMessage() or "request failed"
        err_type = getattr(getattr(failure, "value", None), "__class__", type("E", (), {})).__name__
        if err_type and err_type != "E":
            reason = f"{err_type}: {err_msg}"
        else:
            reason = err_msg

        self.write_failed(req.url, kind, source_url=src_url, reason=reason)
        self.logger.warning("FAILED request: %s (%s)", req.url, err_msg)

    def parse_download(self, response: scrapy.http.Response):
        kind = response.meta.get("kind", "download")
        src_url = response.meta.get("source_url", response.url)

        if response.status == 200 and is_html_response(response):
            cands = response.meta.get("dl_cands") or []
            i = int(response.meta.get("dl_i") or 0)

            if cands and i + 1 < len(cands):
                yield scrapy.Request(
                    cands[i + 1],
                    callback=self.parse_download,
                    errback=self.on_link_error,
                    meta={**response.meta, "dl_i": i + 1},
                    dont_filter=True,
                )
                return

            if self._should_playwright_retry(response):
                pw_req = self._playwright_retry_request(response)
                if pw_req is not None:
                    yield pw_req
                    return

            self.write_failed(
                response.url,
                kind,
                source_url=src_url,
                reason=self._reason_from_response(response),
            )
            return

        if response.status != 200:
            cands = response.meta.get("dl_cands") or []
            i = int(response.meta.get("dl_i") or 0)

            if cands and i + 1 < len(cands):
                yield scrapy.Request(
                    cands[i + 1],
                    callback=self.parse_download,
                    errback=self.on_link_error,
                    meta={**response.meta, "dl_i": i + 1},
                    dont_filter=True,
                )
                return

            if self._should_playwright_retry(response):
                pw_req = self._playwright_retry_request(response)
                if pw_req is not None:
                    yield pw_req
                    return

            self.write_failed(
                response.url,
                kind,
                source_url=src_url,
                reason=self._reason_from_response(response),
            )
            return

        if not self._inc_and_check_limit():
            return

        ext = ext_from_response(response)
        fp = save_bytes(self.out_dir, seq=self.count, url=response.url, body=response.body, ext=ext)
        yield {"saved_path": str(fp)}

    @staticmethod
    def _is_cssf_listing_page(url: str) -> bool:
        return "/en/regulatory-framework" in url and "/wp-content/" not in url

    @staticmethod
    def _infer_language(label_text: str, href: str, item_title: str) -> str:
        t = (item_title or "").lower()
        if "only in french" in t or "uniquement en français" in t:
            return "fr"

        lt = (label_text or "").strip().lower()
        if lt.startswith("link"):
            return "en"
        if lt.startswith("lien"):
            return "fr"

        p = (urlparse(href).path or "").lower()

        if re.search(r"eng(?=\.)", p):
            return "en"
        if re.search(r"(^|[_\-.])en([_\-.]|$)", p):
            return "en"
        if re.search(r"(^|[_\-.])fr([_\-.]|$)", p) or re.search(r"(^|[_\-.])fra([_\-.]|$)", p):
            return "fr"
        if re.search(r"(^|[_\-.])de([_\-.]|$)", p) or re.search(r"(^|[_\-.])deu([_\-.]|$)", p):
            return "de"

        if "/en/" in p:
            return "en"
        if "/fr/" in p:
            return "fr"
        if "/de/" in p:
            return "de"

        return "unknown"

    def _pick_best_link(self, candidates: list[dict]) -> dict:
        return sorted(candidates, key=lambda c: self.LANG_PRIORITY.get(c["lang"], 99))[0]

    @staticmethod
    def _classify_kind(label_text: str, url: str) -> str:
        t = (label_text or "").strip().lower()
        if t.startswith("link"):
            return "link"
        if t.startswith("lien"):
            return "lien"

        p = (urlparse(url).path or "").lower()
        if p.endswith(DOWNLOAD_EXTS):
            return "download"

        if t.startswith(("pdf", "xlsx", "xls", "doc", "docx", "ppt", "pptx", "zip")):
            return "download"

        return "download"

    def _patch_request(self, req: scrapy.Request, *, source_url: str, kind: str) -> scrapy.Request:
        req.meta.setdefault("source_url", source_url)
        req.meta.setdefault("kind", kind)
        if getattr(req, "errback", None) is None:
            req.errback = self.on_link_error
        return req

    def _yield_patched(self, out, *, source_url: str, kind: str) -> Iterator[object]:
        if out is None:
            return
        if isinstance(out, scrapy.Request):
            yield self._patch_request(out, source_url=source_url, kind=kind)
            return
        if isinstance(out, Iterable):
            for x in out:
                if isinstance(x, scrapy.Request):
                    yield self._patch_request(x, source_url=source_url, kind=kind)
                else:
                    yield x
            return
        yield out

    def parse(self, response: scrapy.http.Response):
        ctype = response.headers.get(b"Content-Type", b"").decode("utf-8").lower()
        if not (ctype.startswith("text/html") and self._is_cssf_listing_page(response.url)):
            yield from self.save_download_response(response, kind="download")
            return

        title_links = response.xpath(
            "//h2/a[@href] | //h3/a[@href] | //h4/a[@href] | //h5/a[@href] | //h6/a[@href]"
        )

        for a in title_links:
            item_title = " ".join([t.strip() for t in a.xpath(".//text()").getall() if t.strip()])
            item_url = response.urljoin(a.attrib.get("href", "").strip())

            container = a.xpath(
                "ancestor::*[self::article or self::li or self::div]"
                "[.//a[normalize-space() and ("
                "starts-with(normalize-space(.), 'PDF') or "
                "starts-with(normalize-space(.), 'Lien') or "
                "starts-with(normalize-space(.), 'Link') or "
                "starts-with(normalize-space(.), 'XLSX') or "
                "starts-with(normalize-space(.), 'DOC') or "
                "starts-with(normalize-space(.), 'DOCX') or "
                "starts-with(normalize-space(.), 'PPT') or "
                "starts-with(normalize-space(.), 'PPTX') or "
                "starts-with(normalize-space(.), 'ZIP')"
                ")]]"
                "[1]"
            )
            if not container:
                continue
            container = container[0]

            candidates: list[dict] = []
            for link in container.xpath(".//a[@href]"):
                label_text = " ".join([t.strip() for t in link.xpath(".//text()").getall() if t.strip()])
                if not label_text or not self.RIGHT_LABEL_RE.match(label_text):
                    continue

                href = (link.attrib.get("href") or "").strip()
                if not href:
                    continue
                url = response.urljoin(href)

                if url == item_url:
                    continue

                lang = self._infer_language(label_text, url, item_title)
                candidates.append({"url": url, "label": label_text, "lang": lang})

            if not candidates:
                continue

            best = self._pick_best_link(candidates)
            kind = self._classify_kind(best["label"], best["url"])

            self.write_all(best["url"], kind, source_url=best["url"])

            if kind in ("link", "lien"):
                org = classify_org(best["url"])
                handler = self.handlers.get(org)
                if handler is None:
                    self.write_failed(
                        attempted_url=best["url"],
                        kind=kind,
                        source_url=best["url"],
                        reason=f"No handler registered for org '{org}'",
                    )
                    continue

                try:
                    out = handler.start(self, best["url"], kind, best["url"])
                except TypeError:
                    out = handler.start(self, best["url"], kind)

                yield from self._yield_patched(out, source_url=best["url"], kind=kind)
                continue

            # If you enable direct downloads here, do it via make_download_request()
            # yield self.make_download_request(best["url"], kind="download", source_url=best["url"])

        next_page = (
            response.xpath("//a[@title='Next page']/@href").get()
            or response.xpath("//a[@rel='next']/@href").get()
        )
        if next_page:
            yield scrapy.Request(response.urljoin(next_page), callback=self.parse)
