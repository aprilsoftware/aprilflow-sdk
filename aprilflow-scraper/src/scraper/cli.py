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

import argparse
import os
from pathlib import Path

from scraper.config import load_aprilflow_config
from scraper.state import AppState

from aprilflow import AprilFlowClient


from scraper.handlers.collections import list_collections, use_collection, get_collection
from scraper.handlers.scrape import scrape_profile
from scraper.handlers.upload import upload
from scraper.shell import run_shell


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog="scraper",
        description="April Flow content scraping CLI",
    )
    p.add_argument(
        "--data-dir",
        default=os.environ.get("SCRAPER_DATA_DIR", str(Path.home() / "scraper")),
        help="Local working directory for scraper (default: ~/scraper or $SCRAPER_DATA_DIR)",
    )

    sub = p.add_subparsers(dest="cmd", required=True)

    # ---- collections ----
    p_col = sub.add_parser("collections", help="Collections")
    col_sub = p_col.add_subparsers(dest="collections_cmd", required=True)

    p_col_list = col_sub.add_parser("list", help="List collections available")
    p_col_list.set_defaults(_fn="collections_list")

    p_col_use = col_sub.add_parser("use", help="Set current collection id")
    p_col_use.add_argument("collection_id", help="Collection UUID")
    p_col_use.set_defaults(_fn="collections_use")

    p_col_get = col_sub.add_parser("get", help="Get a collection by id")
    p_col_get.add_argument("collection_id", help="Collection UUID")
    p_col_get.set_defaults(_fn="collections_get")

    # ---- scrape ----
    p_scrape = sub.add_parser("scrape", help="Scrape a profile/source")
    p_scrape.add_argument("profile", help="Profile name")
    p_scrape.add_argument("--limit", type=int, default=None, help="Max items/pages to scrape (default: no limit)")
    p_scrape.set_defaults(_fn="scrape_profile")

    # ---- upload ----
    p_upload = sub.add_parser("upload", help="Upload files to April Flow")
    p_upload.add_argument("run_dir", help="Path to run folder (e.g. ~/scraper/runs/<run>)")
    p_upload.add_argument("--into", "-c", dest="into", help="Target collection UUID")
    p_upload.add_argument("--concurrency", type=int, default=2, help="How many uploads to keep in-flight (default: 2)")
    p_upload.set_defaults(_fn="upload")

    # ---- shell ----
    p_shell = sub.add_parser("shell", help="Interactive mode")
    p_shell.set_defaults(_fn="shell")

    return p


def _build_state(data_dir: Path) -> AppState:
    config = load_aprilflow_config()
    client = AprilFlowClient.create(
        base_url=config.base_url,
        user_key=config.user_key,
    )

    return AppState(
        data_dir=data_dir,
        config=config,
        aprilflow=client,
        current_collection_id=None,
    )


def main(argv: list[str] | None = None) -> None:
    parser = build_parser()
    args = parser.parse_args(argv)

    data_dir = Path(args.data_dir)
    data_dir.mkdir(parents=True, exist_ok=True)

    state = _build_state(data_dir)

    try:
        fn = getattr(args, "_fn", None)

        if fn == "collections_list":
            raise SystemExit(list_collections(state))
        if fn == "collections_use":
            raise SystemExit(use_collection(state, args.collection_id))
        if fn == "collections_get":
            raise SystemExit(get_collection(state, args.collection_id))

        if fn == "scrape_profile":
            raise SystemExit(
                scrape_profile(
                    state,
                    profile=args.profile,
                    limit=args.limit,
                )
            )
        
        if fn == "upload":
            raise SystemExit(
                upload(
                    state,
                    Path(args.run_dir),
                    into=args.into,
                    concurrency=args.concurrency,
                )
            )

        if fn == "shell":
            raise SystemExit(run_shell(state))

        raise SystemExit(2)

    finally:
        try:
            state.aprilflow.close()
        except Exception:
            pass
