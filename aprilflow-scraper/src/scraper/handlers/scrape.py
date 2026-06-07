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

from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

from scraper.state import AppState


@dataclass
class RunDirs:
    root: Path
    scraped: Path
    uploading: Path
    uploaded: Path
    failed: Path


def _make_run_dirs(base: Path, profile: str) -> RunDirs:
    ts = datetime.now().strftime("%Y-%m-%d_%H%M%S")
    root = base / "runs" / f"{ts}_{profile}"
    scraped = root / "scraped"
    uploading = root / "uploading"
    uploaded = root / "uploaded"
    failed = root / "failed"
    for p in (scraped, uploading, uploaded, failed):
        p.mkdir(parents=True, exist_ok=True)
    return RunDirs(root, scraped, uploading, uploaded, failed)


def _load_profile(profile: str):
    import importlib
    try:
        return importlib.import_module(f"scraper.profiles.{profile}")
    except ModuleNotFoundError:
        return None


def scrape_profile(
    state: AppState,
    profile: str,
    limit: int | None = None,
) -> int:
    state.data_dir.mkdir(parents=True, exist_ok=True)
    run = _make_run_dirs(state.data_dir, profile)

    mod = _load_profile(profile)
    if mod is None or not hasattr(mod, "scrape"):
        print(f"Unknown profile '{profile}'. Expected scraper.profiles.{profile} with scrape(out_dir, limit=...).")
        return 2

    print(f"Scraping profile '{profile}' into: {run.scraped}")
    scraped_files: list[Path] = mod.scrape(run.scraped, limit=limit)
    print(f"Scraped {len(scraped_files)} file(s).")

    return 0
