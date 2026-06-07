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

import scrapy
from scrapy_playwright.page import PageMethod

from scraper.profiles.cssf_spider.common import save_bytes


class LegiluxHandler:
    org = "legilux"

    def classify_kind(self, label_text: str, url: str) -> str:
        return "lien"

    def start(self, spider, url: str, kind: str) -> scrapy.Request:
        return scrapy.Request(
            url,
            callback=self.handle,
            errback=spider.on_link_error,
            cb_kwargs={"spider": spider, "src_url": url, "kind": kind},
            meta={
                "source_url": url,
                "kind": kind,
                "playwright": True,
                "playwright_include_page": True,
                "playwright_page_goto_kwargs": {"wait_until": "domcontentloaded", "timeout": 90_000},
                "playwright_page_methods": [
                    PageMethod("wait_for_timeout", 800),
                ],
                "legilux_retry": 0,
            },
            dont_filter=True,
        )

    async def handle(self, response: scrapy.http.Response, spider, src_url: str, kind: str):
        page = response.meta.get("playwright_page")

        try:
            if page is None:
                self._fail(spider, src_url, kind, f"HTTP {response.status} (no playwright page)")
                return

            try:
                await page.evaluate(
                    """() => {
                        const selectors = [
                          '#onetrust-banner-sdk',
                          '.onetrust-pc-dark-filter',
                          '.ot-sdk-container',
                          '.otFloatingRoundedCorner'
                        ];
                        for (const sel of selectors) {
                          const el = document.querySelector(sel);
                          if (el) el.remove();
                        }
                    }"""
                )
            except Exception:
                pass

            try:
                await page.wait_for_function(
                    "() => (document.body && (document.body.innerText || '').trim().length > 500)",
                    timeout=20_000,
                )
            except Exception:
                pass

            await self._try_reveal_text(page)

            extracted = await self._extract_best_fragment_from_page(page, src_url)
            if not extracted:
                extracted = await self._extract_from_best_frame(page, src_url)

            if not extracted:
                extracted = await self._extract_relaxed_fragment(page, src_url)

            if not extracted:
                self._fail(spider, src_url, kind, "Could not locate main content container (all strategies failed)")
                return

            if not spider._inc_and_check_limit():
                return

            pdf_bytes = await self._render_fragment_to_pdf(page, src_url, extracted["title"], extracted["html"])

            if len(pdf_bytes) < 20_000 and int(response.meta.get("legilux_retry") or 0) < 1:
                spider.count -= 1
                meta = dict(response.meta)
                meta["legilux_retry"] = 1
                yield scrapy.Request(
                    src_url,
                    callback=self.handle,
                    errback=spider.on_link_error,
                    cb_kwargs={"spider": spider, "src_url": src_url, "kind": kind},
                    meta=meta,
                    dont_filter=True,
                )
                return

            fp = save_bytes(
                spider.out_dir,
                seq=spider.count,
                url=src_url,
                body=pdf_bytes,
                ext="pdf",
            )
            yield {"saved_path": str(fp), "source_url": src_url, "url": response.url}

        except Exception as e:
            self._fail(spider, src_url, kind, f"Legilux content-only PDF failed: {type(e).__name__}: {e}")

        finally:
            if page is not None:
                try:
                    await page.close()
                except Exception:
                    pass

    async def _try_reveal_text(self, page):
        """
        Click common controls that reveal the document text.
        This is intentionally conservative: try a few selectors/texts, ignore failures.
        """
        candidates = [
            "text=Afficher le texte",
            "text=Afficher le texte consolidé",
            "text=Texte consolidé",
            "text=Texte coordonné",
            "text=Texte",
            "text=Voir le texte",
            "text=Version consolidée",
            "text=Version coordonnée",
            "text=Show text",
            "text=Consolidated text",
            "text=Full text",
        ]

        for sel in candidates:
            try:
                loc = page.locator(sel).first
                if await loc.count() > 0:
                    await loc.click(timeout=1500)
                    await page.wait_for_timeout(800)
            except Exception:
                continue

    async def _extract_best_fragment_from_page(self, page, base_url: str):
        """
        Strict extraction (good for the 23 working links).
        Avoids header/banner such as 'Journal officiel du Grand-Duché de Luxembourg'.
        """
        return await page.evaluate(
            """(baseUrl) => {
                const BAD_PHRASES = [
                  "journal officiel du grand-duché de luxembourg",
                  "journal officiel du grand-duche de luxembourg",
                  "grand-duché de luxembourg",
                  "grand-duche de luxembourg",
                  "menu",
                  "navigation",
                  "recherche",
                  "search",
                  "connexion",
                  "login",
                  "cookies"
                ];

                function norm(s) {
                  return (s || "").toString().toLowerCase().replace(/\\s+/g, " ").trim();
                }
                function hasBadPhrase(text) {
                  const t = norm(text);
                  return BAD_PHRASES.some(p => t.includes(p));
                }
                function looksLikeChrome(el) {
                  if (!el) return true;
                  const id = norm(el.id);
                  const cls = norm(el.className);
                  const tag = (el.tagName || "").toLowerCase();

                  if (tag === "header" || tag === "footer" || tag === "nav" || tag === "aside") return true;
                  if (id.includes("header") || id.includes("footer") || id.includes("nav") || id.includes("menu")) return true;
                  if (cls.includes("header") || cls.includes("footer") || cls.includes("nav") || cls.includes("menu")) return true;
                  if (cls.includes("breadcrumb") || cls.includes("breadcrumbs")) return true;
                  return false;
                }

                function score(el) {
                  if (!el) return -1;
                  if (looksLikeChrome(el)) return -1;

                  const text = (el.innerText || "").trim();
                  if (text.length < 1400) return -1;                 // strict threshold
                  if (hasBadPhrase(text) && text.length < 9000) return -1;  // avoid header/banner blocks

                  const h = el.querySelectorAll("h1,h2,h3,h4").length;
                  const p = el.querySelectorAll("p").length;
                  const li = el.querySelectorAll("li").length;
                  const tables = el.querySelectorAll("table").length;

                  let s = text.length + (h * 800) + (p * 80) + (li * 30) + (tables * 200);

                  const links = el.querySelectorAll("a").length;
                  if (links > 120) s -= 2500;

                  return s;
                }

                const selectors = [
                  "main article",
                  "article",
                  "main",
                  "#content",
                  "#main-content",
                  ".content",
                  ".page-content",
                  ".region-content",
                  ".layout-content",
                ];

                const candidates = [];
                for (const sel of selectors) {
                  document.querySelectorAll(sel).forEach(el => candidates.push(el));
                }
                // Add sizeable blocks
                document.querySelectorAll("section, div").forEach(el => {
                  const tlen = ((el.innerText || "").trim()).length;
                  if (tlen >= 1600) candidates.push(el);
                });

                // Dedup by reference
                const uniq = [];
                const seen = new Set();
                for (const el of candidates) {
                  if (!seen.has(el)) { seen.add(el); uniq.push(el); }
                }

                let best = null;
                let bestScore = -1;
                for (const el of uniq) {
                  const s = score(el);
                  if (s > bestScore) { best = el; bestScore = s; }
                }

                const title = (document.querySelector("h1")?.innerText || document.title || "").trim();

                if (!best) return null;

                const clone = best.cloneNode(true);
                const kill = [
                  "header", "footer", "nav", "aside",
                  ".breadcrumb", ".breadcrumbs",
                  ".share", ".social", ".toolbar",
                  ".pagination", ".pager",
                  ".menu", ".nav", ".navbar", ".sidebar"
                ];
                for (const k of kill) clone.querySelectorAll(k).forEach(n => n.remove());

                return { title, html: clone.outerHTML };
            }""",
            base_url,
        )

    async def _extract_from_best_frame(self, page, base_url: str):
        try:
            frames = page.frames
        except Exception:
            frames = []

        candidates = []
        for fr in frames:
            try:
                if fr == page.main_frame:
                    continue
                txt = await fr.evaluate("() => (document.body && (document.body.innerText || '').trim().length) || 0")
                candidates.append((txt, fr))
            except Exception:
                continue

        if not candidates:
            return None

        candidates.sort(key=lambda x: x[0], reverse=True)
        best_len, best_frame = candidates[0]
        if best_len < 800:
            return None

        try:
            extracted = await best_frame.evaluate(
                """() => {
                    const title =
                      (document.querySelector("h1")?.innerText || document.title || "").trim();

                    function norm(s){ return (s||"").toString().toLowerCase().replace(/\\s+/g," ").trim(); }
                    function looksLikeChrome(el){
                      if(!el) return true;
                      const tag = (el.tagName||"").toLowerCase();
                      const id = norm(el.id);
                      const cls = norm(el.className);
                      if (tag==="header"||tag==="footer"||tag==="nav"||tag==="aside") return true;
                      if (id.includes("nav")||id.includes("menu")) return true;
                      if (cls.includes("nav")||cls.includes("menu")||cls.includes("sidebar")) return true;
                      return false;
                    }
                    function score(el){
                      if(!el || looksLikeChrome(el)) return -1;
                      const text = (el.innerText||"").trim();
                      if(text.length < 800) return -1;
                      const p = el.querySelectorAll("p").length;
                      const h = el.querySelectorAll("h1,h2,h3").length;
                      return text.length + (p*60) + (h*600);
                    }

                    const sels = ["main","article","#content","#main-content",".content",".page-content",".region-content",".layout-content"];
                    let best=null, bestScore=-1;
                    for (const sel of sels){
                      const el = document.querySelector(sel);
                      const s = score(el);
                      if(s>bestScore){best=el; bestScore=s;}
                    }
                    if(!best){
                      const blocks = Array.from(document.querySelectorAll("section,div"))
                        .filter(el => ((el.innerText||"").trim().length >= 900));
                      for (const el of blocks){
                        const s=score(el);
                        if(s>bestScore){best=el; bestScore=s;}
                      }
                    }
                    if(!best) return null;

                    const clone = best.cloneNode(true);
                    ["header","footer","nav","aside",".breadcrumb",".menu",".nav",".sidebar"].forEach(k=>{
                      clone.querySelectorAll(k).forEach(n=>n.remove());
                    });

                    return { title, html: clone.outerHTML };
                }"""
            )
            return extracted
        except Exception:
            return None

    async def _extract_relaxed_fragment(self, page, base_url: str):
        """
        Last resort: relax thresholds and accept the largest non-chrome block.
        This prevents false negatives on pages with less text or different DOM.
        """
        return await page.evaluate(
            """() => {
                function norm(s){ return (s||"").toString().toLowerCase().replace(/\\s+/g," ").trim(); }
                function looksLikeChrome(el){
                  if(!el) return true;
                  const tag = (el.tagName||"").toLowerCase();
                  const id = norm(el.id);
                  const cls = norm(el.className);
                  if (tag==="header"||tag==="footer"||tag==="nav"||tag==="aside") return true;
                  if (id.includes("header")||id.includes("footer")||id.includes("nav")||id.includes("menu")) return true;
                  if (cls.includes("header")||cls.includes("footer")||cls.includes("nav")||cls.includes("menu")||cls.includes("sidebar")) return true;
                  return false;
                }
                function score(el){
                  if(!el || looksLikeChrome(el)) return -1;
                  const text=(el.innerText||"").trim();
                  if(text.length < 500) return -1;
                  const p=el.querySelectorAll("p").length;
                  const h=el.querySelectorAll("h1,h2,h3").length;
                  return text.length + (p*40) + (h*500);
                }

                const title=(document.querySelector("h1")?.innerText||document.title||"").trim();

                let best=null, bestScore=-1;
                const blocks = Array.from(document.querySelectorAll("main,article,section,div"));
                for (const el of blocks){
                  const s=score(el);
                  if(s>bestScore){best=el; bestScore=s;}
                }
                if(!best) return null;

                const clone=best.cloneNode(true);
                ["header","footer","nav","aside",".breadcrumb",".menu",".nav",".sidebar"].forEach(k=>{
                  clone.querySelectorAll(k).forEach(n=>n.remove());
                });
                return { title, html: clone.outerHTML };
            }"""
        )

    async def _render_fragment_to_pdf(self, page, src_url: str, title: str, html_fragment: str) -> bytes:
        """
        Render extracted fragment into a clean new page and print to PDF.
        """
        ctx = page.context
        content_page = await ctx.new_page()
        try:
            await content_page.emulate_media(media="screen")

            title = (title or "").strip()
            composed_html = f"""<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <base href="{src_url}" />
  <title>{title}</title>
  <style>
    html, body {{ margin: 0; padding: 0; font-family: Arial, sans-serif; }}
    body {{ padding: 18mm 14mm; }}
    .doc-title {{ font-size: 18pt; font-weight: 700; margin: 0 0 10mm 0; }}
    .doc-wrap {{ max-width: 180mm; margin: 0 auto; }}
    nav, header, footer, aside {{ display: none !important; }}
    table {{ border-collapse: collapse; width: 100%; }}
    td, th {{ border: 1px solid #ddd; padding: 4px 6px; vertical-align: top; }}
    @media print {{
      body {{ padding: 12mm 10mm; }}
    }}
  </style>
</head>
<body>
  <div class="doc-wrap">
    {f'<div class="doc-title">{title}</div>' if title else ''}
    {html_fragment}
  </div>
</body>
</html>
"""
            await content_page.set_content(composed_html, wait_until="domcontentloaded")
            await content_page.wait_for_timeout(900)

            return await content_page.pdf(
                format="A4",
                print_background=True,
                prefer_css_page_size=True,
                margin={"top": "0mm", "bottom": "0mm", "left": "0mm", "right": "0mm"},
            )
        finally:
            try:
                await content_page.close()
            except Exception:
                pass

    @staticmethod
    def _fail(spider, url: str, kind: str, reason: str):
        if hasattr(spider, "write_failed"):
            try:
                spider.write_failed(attempted_url=url, kind=kind, source_url=url, reason=reason)
            except TypeError:
                spider.write_failed(url, kind)
