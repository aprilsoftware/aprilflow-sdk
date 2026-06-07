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

import cmd
import shlex
from pathlib import Path

from scraper.state import AppState
from scraper.handlers.collections import list_collections, use_collection, get_collection
from scraper.handlers.scrape import scrape_profile
from scraper.handlers.upload import upload


class ScraperShell(cmd.Cmd):
    intro = "scraper shell. Type 'help' for commands.\n"

    def __init__(self, state: AppState) -> None:
        super().__init__()
        self.state = state

    @property
    def prompt(self) -> str:
        cid = self.state.current_collection_id
        short = cid[:8] if cid else "-"
        return f"scraper[{short}]> "

    def emptyline(self) -> bool:
        return False

    def do_quit(self, arg: str) -> bool:
        """Quit the shell."""
        return True

    def do_exit(self, arg: str) -> bool:
        """Exit the shell."""
        return True

    def do_status(self, arg: str) -> None:
        """Show current session status (selected collection, etc.)."""
        cid = self.state.current_collection_id
        if not cid:
            print("Current collection: (none)")
            print("Tip: use <collection-id>  OR  collections use <collection-id>")
            return

        print(f"Current collection: {cid}")
        try:
            from scraper.handlers.collections import _get_collection
            c = _get_collection(self.state, cid)
            name = c.name
            if name:
                print(f"Name: {name}")
        except Exception:
            pass

    def do_current(self, arg: str) -> None:
        """Alias for status."""
        self.do_status(arg)

    def do_collections(self, arg: str) -> None:
        """Collections commands: list, get <id>, use <id>."""
        parts = shlex.split(arg)
        if not parts:
            print("Usage: collections [list|get|use] ...")
            return

        sub = parts[0]

        if sub == "list":
            list_collections(self.state)
            return

        if sub == "get":
            if len(parts) != 2:
                print("Usage: collections get <collection-id>")
                return
            get_collection(self.state, parts[1])
            return

        if sub == "use":
            if len(parts) != 2:
                print("Usage: collections use <collection-id>")
                return
            use_collection(self.state, parts[1])
            return

        print(f"Unknown collections subcommand: {sub}")

    def do_use(self, arg: str) -> None:
        """Shortcut to select a current collection: use <collection-id>."""
        parts = shlex.split(arg)
        if len(parts) != 1:
            print("Usage: use <collection-id>")
            return
        use_collection(self.state, parts[0])

    def do_scrape(self, arg: str) -> None:
        """
        Scrape a profile and upload results:
          scrape <profile> [--limit N]
        """
        parts = shlex.split(arg)
        if not parts:
            print("Usage: scrape <profile> [--limit N]")
            return

        profile = parts[0]
        limit: int | None = None

        i = 1
        while i < len(parts):
            token = parts[i]
            if token == "--limit":
                i += 1
                if i >= len(parts):
                    print("Missing value after --limit")
                    return
                limit = int(parts[i])
            else:
                print(f"Unknown option: {token}")
                return
            i += 1

        scrape_profile(self.state, profile=profile, limit=limit)

    def do_upload(self, arg: str) -> None:
        """
        Upload results:
          upload <run_dir> [--into <id>] [--concurrency N]
        """
        parts = shlex.split(arg)
        if not parts:
            print("Usage: upload <run_dir> [--into <id>] [--concurrency N]")
            return

        run_dir = parts[0]
        into: str | None = None
        concurrency: int = 2

        i = 1
        while i < len(parts):
            token = parts[i]
            if token in ("--into", "-c"):
                i += 1
                if i >= len(parts):
                    print("Missing value after --into")
                    return
                into = parts[i]
            elif token == "--concurrency":
                i += 1
                if i >= len(parts):
                    print("Missing value after --concurrency")
                    return
                concurrency = int(parts[i])
            else:
                print(f"Unknown option: {token}")
                return
            i += 1

        upload(self.state, Path(run_dir), into=into, concurrency=concurrency)


def run_shell(state: AppState) -> int:
    ScraperShell(state).cmdloop()
    return 0
