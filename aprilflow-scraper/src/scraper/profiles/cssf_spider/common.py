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
import hashlib
import csv
import re
from urllib.parse import urlparse

import scrapy
from scrapy.http import TextResponse


ORG_MAP: list[tuple[str, str]] = [
    ("cssf.lu", "cssf"),

    ("europa.eu", "eu-commission"),

    ("www.un.org", "un"),
    ("un.org", "un"),
    ("press.un.org", "un"),

    ("data.legilux.public.lu", "legilux"),
    ("legilux.public.lu", "legilux"),
    ("www.legilux.public.lu", "legilux"),

    #("data.legilux.public.lu", "legilux"),
    #("legilux.public.lu", "legilux"),
    #("www.legilux.public.lu", "legilux"),
    #("mfin.gouvernement.lu", "lux-mfin"),

    #("www.iosco.org", "iosco"),
    #("iosco.org", "iosco"),
    #("www.ngfs.net", "ngfs"),
    #("ngfs.net", "ngfs"),

    #("www.google.com", "google"),
    #("google.com", "google"),


    #("www.undocs.org", "undocs"),
    #("undocs.org", "undocs"),
]

DOWNLOAD_EXTS = (".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".zip")


def domain(url: str) -> str:
    return (urlparse(url).netloc or "").lower()


def classify_org(url: str) -> str:
    host = domain(url)
    for h, org in ORG_MAP:
        if host == h or host.endswith("." + h):
            return org
    return (host.replace("www.", "") or "unknown").replace(":", "_")


def looks_like_file(u: str) -> bool:
    return (urlparse(u).path or "").lower().endswith(DOWNLOAD_EXTS)


def is_html_response(response: scrapy.http.Response) -> bool:
    if isinstance(response, TextResponse):
        return True
    ctype = response.headers.get(b"Content-Type", b"").decode("utf-8").lower()
    return ctype.startswith("text/html") or "text/html" in ctype


def extract_file_links(response: scrapy.http.Response) -> list[str]:
    if not is_html_response(response):
        return []
    seen = set()
    out: list[str] = []
    for href in response.css("a::attr(href)").getall():
        if not href:
            continue
        u = response.urljoin(href)
        if looks_like_file(u) and u not in seen:
            seen.add(u)
            out.append(u)
    return out


def infer_lang_from_url(u: str) -> str:
    p = (urlparse(u).path or "").lower()
    if "/en/" in p:
        return "en"
    if "/fr/" in p:
        return "fr"
    if "/de/" in p:
        return "de"

    if re.search(r"eng(?=\.)", p) or re.search(r"(^|[_\-.])en([_\-.]|$)", p):
        return "en"
    if re.search(r"(^|[_\-.])fr([_\-.]|$)", p) or re.search(r"(^|[_\-.])fra([_\-.]|$)", p):
        return "fr"
    if re.search(r"(^|[_\-.])de([_\-.]|$)", p) or re.search(r"(^|[_\-.])deu([_\-.]|$)", p):
        return "de"

    return "unknown"


def pick_best_doc(urls: list[str]) -> str:
    priority = {"en": 0, "fr": 1, "de": 2, "unknown": 99}
    return sorted(urls, key=lambda u: priority.get(infer_lang_from_url(u), 99))[0]


def ext_from_response(response: scrapy.http.Response) -> str:
    ctype = response.headers.get(b"Content-Type", b"").decode("utf-8").lower()
    if ctype.startswith("application/pdf"):
        return "pdf"
    if ctype.startswith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"):
        return "xlsx"
    if ctype.startswith("application/vnd.ms-excel"):
        return "xls"
    if ctype.startswith("application/vnd.openxmlformats-officedocument.wordprocessingml.document"):
        return "docx"
    if ctype.startswith("application/msword"):
        return "doc"
    if ctype.startswith("application/vnd.openxmlformats-officedocument.presentationml.presentation"):
        return "pptx"
    if ctype.startswith("application/vnd.ms-powerpoint"):
        return "ppt"
    if ctype.startswith("application/zip") or "zip" in ctype:
        return "zip"
    if ctype.startswith("text/html"):
        return "html"

    path = (urlparse(response.url).path or "").lower()
    for ext in ("pdf", "xlsx", "xls", "docx", "doc", "pptx", "ppt", "zip", "html"):
        if path.endswith(f".{ext}"):
            return ext
    return "bin"


class CsvWriters:
    """
    links_all.csv:
      - source_url: origin URL discovered on CSSF listing (or the original link for this chain)
      - url:        the URL we logged at discovery time (often same as source_url)

    links_failed.csv:
      - source_url:    origin URL discovered on CSSF listing (or the original link for this chain)
      - attempted_url: the actual URL that was requested when it failed
      - reason:        short explanation of why the request failed
    """

    def __init__(self, out_dir: Path):
        self.csv_all_path = out_dir / "links_all.csv"
        self.csv_failed_path = out_dir / "links_failed.csv"

        self._fp_all = self.csv_all_path.open("w", newline="", encoding="utf-8")
        self._w_all = csv.DictWriter(
            self._fp_all,
            fieldnames=["location", "source_url", "url", "kind"],
        )
        self._w_all.writeheader()
        self._fp_all.flush()

        self._fp_failed = self.csv_failed_path.open("w", newline="", encoding="utf-8")
        self._w_failed = csv.DictWriter(
            self._fp_failed,
            fieldnames=["location", "source_url", "attempted_url", "kind", "reason"],
        )
        self._w_failed.writeheader()
        self._fp_failed.flush()

    def close(self):
        for fp in (self._fp_all, self._fp_failed):
            try:
                fp.flush()
                fp.close()
            except Exception:
                pass

    def write_all(self, url: str, kind: str, *, source_url: str | None = None):
        src = source_url or url
        self._w_all.writerow(
            {"location": classify_org(src), "source_url": src, "url": url, "kind": kind}
        )
        self._fp_all.flush()

    def write_failed(
        self,
        attempted_url: str,
        kind: str,
        *,
        reason: str,
        source_url: str | None = None,
    ):
        src = source_url or attempted_url
        self._w_failed.writerow(
            {
                "location": classify_org(src),
                "source_url": src,
                "attempted_url": attempted_url,
                "kind": kind,
                "reason": reason,
            }
        )
        self._fp_failed.flush()


def save_bytes(out_dir: Path, *, seq: int, url: str, body: bytes, ext: str) -> Path:
    org = classify_org(url)
    org_dir = out_dir / org
    org_dir.mkdir(parents=True, exist_ok=True)

    h = hashlib.sha256(url.encode("utf-8")).hexdigest()[:16]
    fp = org_dir / f"{seq:05d}_{h}.{ext}"
    fp.write_bytes(body)
    return fp
