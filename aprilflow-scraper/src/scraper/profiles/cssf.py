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

from scrapy.crawler import CrawlerProcess
from scrapy import signals
from pydispatch import dispatcher

from scraper.profiles.cssf_spider.spider import CssfFrameworkSpider

def scrape(out_dir: Path, *, limit: int | None = None) -> list[Path]:
    saved: list[Path] = []

    def _on_item_scraped(item, response, spider):
        p = item.get("saved_path")
        if p:
            saved.append(Path(p))

    dispatcher.connect(_on_item_scraped, signal=signals.item_scraped)

    settings = {
        "LOG_ENABLED": True,
        "LOG_LEVEL": "INFO",
        "ROBOTSTXT_OBEY": False,
        "DOWNLOAD_DELAY": 0.25,
        "AUTOTHROTTLE_ENABLED": True,
        "AUTOTHROTTLE_TARGET_CONCURRENCY": 1.0,
        "RETRY_ENABLED": True,
        "RETRY_TIMES": 5,
        "COOKIES_ENABLED": True,
        "HTTPERROR_ALLOW_ALL": True,
        "DOWNLOAD_HANDLERS": {
            "http": "scrapy_playwright.handler.ScrapyPlaywrightDownloadHandler",
            "https": "scrapy_playwright.handler.ScrapyPlaywrightDownloadHandler",
        },
        "TWISTED_REACTOR": "twisted.internet.asyncioreactor.AsyncioSelectorReactor",

        "PLAYWRIGHT_LAUNCH_OPTIONS": {
            "headless": True,
        },

        #"PLAYWRIGHT_BROWSER_TYPE": "chromium",
        #"PLAYWRIGHT_LAUNCH_OPTIONS": {
        #    "headless": False,
        #    "channel": "chrome",
        #},

        #"PLAYWRIGHT_CONTEXTS": {
        #    "un_human": {
        #        "user_data_dir": "/home/cedric/.config/google-chrome/Default",
        #        "locale": "en-GB",
        #        "viewport": {"width": 1366, "height": 768},
        #    }
        #},
    }

    process = CrawlerProcess(settings=settings)
    process.crawl(CssfFrameworkSpider, out_dir=str(out_dir), limit=limit)
    process.start()

    dispatcher.disconnect(_on_item_scraped, signal=signals.item_scraped)
    return saved
