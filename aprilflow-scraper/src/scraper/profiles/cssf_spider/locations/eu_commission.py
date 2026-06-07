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

import re
from urllib.parse import (
    urlparse,
    urlencode,
    parse_qs,
    urlunparse,
    quote,
)

import scrapy
from scrapy_playwright.page import PageMethod

from scraper.profiles.cssf_spider.common import (
    DOWNLOAD_EXTS,
    is_html_response,
    extract_file_links,
    pick_best_doc,
)

_LANGS = ("en", "fr", "de")
_LANG_PRIO = {"en": 0, "fr": 1, "de": 2, "unknown": 99}

_FMT_PRIO = {"pdf": 0, "docx": 1, "doc": 2, "rtf": 3, "unknown": 99}


def _infer_lang(u: str) -> str:
    pu = urlparse(u)
    p = (pu.path or "").lower()

    qs = parse_qs(pu.query or "")
    ql = (qs.get("lang") or [None])[0]
    if ql and ql.lower() in _LANGS:
        return ql.lower()

    m = re.search(r"(^|/)(en|fr|de)(/|$)", p)
    if m:
        return m.group(2)

    m2 = re.search(r"/detail/(?P<lang>[a-z]{2})/", p)
    if m2 and m2.group("lang") in _LANGS:
        return m2.group("lang")

    return "unknown"


def _infer_fmt(u: str) -> str:
    p = (urlparse(u).path or "").lower()

    for ext in ("pdf", "docx", "doc", "rtf"):
        if p.endswith(f".{ext}"):
            return ext

    m = re.search(r"/txt/(pdf|docx|doc|rtf)(/|$)", p)
    if m:
        return m.group(1)

    qs = parse_qs(urlparse(u).query or "")
    fn = (qs.get("filename") or [None])[0]
    if fn:
        fn = fn.lower()
        for ext in ("pdf", "docx", "doc", "rtf"):
            if fn.endswith(f".{ext}"):
                return ext

    return "unknown"


def _sort_lang_then_fmt(urls: list[str]) -> list[str]:
    return sorted(
        urls,
        key=lambda u: (
            _LANG_PRIO.get(_infer_lang(u), 99),
            _FMT_PRIO.get(_infer_fmt(u), 99),
            u,
        ),
    )


def _language_variants(url: str) -> list[str]:
    pu = urlparse(url)
    path = pu.path or ""
    qs = parse_qs(pu.query or "")

    out: list[str] = []

    m = re.search(r"(/presscorner/detail/)([a-z]{2})(/.*)$", path, re.I)
    if m:
        pre, _, rest = m.group(1), m.group(2), m.group(3)
        for L in _LANGS:
            out.append(urlunparse(pu._replace(path=f"{pre}{L}{rest}")))
        return out

    if "lang" in qs:
        for L in _LANGS:
            new_qs = dict(qs)
            new_qs["lang"] = [L]
            out.append(
                urlunparse(
                    pu._replace(query=urlencode({k: v[0] for k, v in new_qs.items()}))
                )
            )
        return out

    m2 = re.search(r"^/(en|fr|de)(/.*)$", path, re.I)
    if m2:
        rest = m2.group(2)
        for L in _LANGS:
            out.append(urlunparse(pu._replace(path=f"/{L}{rest}")))
        return out

    return [url]


def _looks_like_download(u: str) -> bool:
    pu = urlparse(u)
    host = (pu.netloc or "").lower()
    p = (pu.path or "").lower()

    if p.endswith(DOWNLOAD_EXTS):
        return True

    q = parse_qs(pu.query or "")
    fn = (q.get("filename") or [None])[0]
    if fn and fn.lower().endswith(DOWNLOAD_EXTS):
        return True

    if "/document/download/" in p or p.endswith("/download") or "/download/" in p:
        return True

    if "eur-lex.europa.eu" in host and re.search(r"/legal-content/[^/]+/txt/(pdf|docx|doc|rtf)/", p):
        return True

    return False


def _extract_celex_from_url(url: str) -> str | None:
    pu = urlparse(url)
    qs = parse_qs(pu.query or "")
    uri = (qs.get("uri") or [None])[0]
    if uri:
        m = re.search(r"CELEX:([^&]+)", uri)
        if m:
            return m.group(1).strip()

    m2 = re.search(r"CELEX:([0-9A-Z][^&]+)", url, re.I)
    if m2:
        return m2.group(1).strip()

    return None


def _eurlex_pdf_candidates(celex: str, lang: str) -> list[str]:
    lang_up = lang.upper()
    celex_q = quote(celex, safe="")  # only the CELEX value
    full_uri = quote(f"CELEX:{celex}", safe=":")

    lexuriserv_pdf = (
        f"https://eur-lex.europa.eu/LexUriServ/LexUriServ.do?uri=CELEX:{celex_q}:{lang_up}:PDF"
    )

    legal_pdf_rid = (
        f"https://eur-lex.europa.eu/legal-content/{lang_up}/TXT/PDF/?rid=2&uri={full_uri}"
    )

    legal_pdf = (
        f"https://eur-lex.europa.eu/legal-content/{lang_up}/TXT/PDF/?uri={full_uri}"
    )

    return [lexuriserv_pdf, legal_pdf_rid, legal_pdf]


def _eurlex_direct_candidates(url: str) -> list[str]:
    pu = urlparse(url)
    host = (pu.netloc or "").lower()
    p = pu.path or ""

    if "eur-lex.europa.eu" not in host:
        return []

    m = re.search(r"^/legal-content/(?P<lang>[A-Za-z]{2})/TXT/?$", p)
    if not m:
        return []

    celex = _extract_celex_from_url(url)
    if not celex:
        return []

    lang = m.group("lang").lower()
    if lang not in _LANGS:
        lang = "en"

    return _eurlex_pdf_candidates(celex, lang)


def _extract_more_file_links(response: scrapy.http.Response) -> list[str]:
    seen = set()
    out: list[str] = []

    for u in _eurlex_direct_candidates(response.url):
        if u not in seen:
            seen.add(u)
            out.append(u)

    for href in response.css("a::attr(href)").getall():
        if not href:
            continue
        u = response.urljoin(href)
        if _looks_like_download(u) and u not in seen:
            seen.add(u)
            out.append(u)

    return out


def _expand_eurlex_download_candidates(url: str) -> list[str] | None:
    pu = urlparse(url)
    host = (pu.netloc or "").lower()
    if "eur-lex.europa.eu" not in host:
        return None

    celex = _extract_celex_from_url(url)
    if not celex:
        return None

    lang = _infer_lang(url)
    if lang not in _LANGS:
        lang = "en"

    return _eurlex_pdf_candidates(celex, lang)


class EuCommissionHandler:
    org = "eu-commission"

    def classify_kind(self, label_text: str, url: str) -> str:
        p = (urlparse(url).path or "").lower()
        if p.endswith(DOWNLOAD_EXTS):
            return "download"
        return "link"

    def start(self, spider, url: str, kind: str) -> scrapy.Request:
        variants = _language_variants(url)
        variants = _sort_lang_then_fmt(variants)
        first = variants[0]

        return scrapy.Request(
            first,
            callback=self.handle,
            errback=spider.on_link_error,
            cb_kwargs={
                "spider": spider,
                "src_url": url,
                "kind": kind,
                "lang_cands": variants,
                "lang_i": 0,
            },
            meta={
                "source_url": url,
                "kind": kind,
                "playwright": True,
                "playwright_page_goto_kwargs": {
                    "wait_until": "domcontentloaded",
                    "timeout": 90_000,
                },
                "playwright_page_methods": [PageMethod("wait_for_timeout", 800)],
            },
            dont_filter=True,
        )

    def handle(
        self,
        response: scrapy.http.Response,
        spider,
        src_url: str,
        kind: str,
        lang_cands: list[str],
        lang_i: int,
    ):
        if response.status != 200:
            if lang_i + 1 < len(lang_cands):
                nxt = lang_cands[lang_i + 1]
                yield scrapy.Request(
                    nxt,
                    callback=self.handle,
                    errback=spider.on_link_error,
                    cb_kwargs={
                        "spider": spider,
                        "src_url": src_url,
                        "kind": kind,
                        "lang_cands": lang_cands,
                        "lang_i": lang_i + 1,
                    },
                    meta={**response.meta, "source_url": src_url, "kind": kind},
                    dont_filter=True,
                )
                return

            spider.write_failed(
                attempted_url=response.url,
                kind=kind,
                source_url=src_url,
                reason=f"HTTP {response.status} after trying {len(lang_cands)} language variants",
            )
            return

        if not is_html_response(response):
            response.meta.setdefault("source_url", src_url)
            response.meta.setdefault("kind", "download")
            yield from spider.save_download_response(response, kind="download")
            return

        files = extract_file_links(response)

        more = _extract_more_file_links(response)
        if more:
            files = list(dict.fromkeys((files or []) + more))

        if files:
            files = _sort_lang_then_fmt(files)

            best = pick_best_doc(files)
            eurlex_cands = _expand_eurlex_download_candidates(best)

            if eurlex_cands:
                eurlex_cands = list(dict.fromkeys(eurlex_cands))
                yield spider.make_download_request(
                    eurlex_cands[0],
                    kind="download",
                    source_url=src_url,
                    meta_extra={
                        "dl_cands": eurlex_cands,
                        "dl_i": 0,
                        "eurlex_celex": _extract_celex_from_url(best) or "",
                    },
                )
                return

            yield spider.make_download_request(best, kind="download", source_url=src_url)
            return

        response.meta.setdefault("source_url", src_url)
        response.meta.setdefault("kind", "download")
        yield from spider.save_download_response(response, kind="download")
