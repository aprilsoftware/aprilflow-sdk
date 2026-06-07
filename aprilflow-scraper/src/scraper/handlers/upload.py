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
from pathlib import Path
import shutil
import time
from concurrent.futures import FIRST_COMPLETED, Future, ThreadPoolExecutor, wait

from aprilflow.models.upload import Upload

from scraper.handlers.collections import _get_collection
from scraper.handlers.scrape import RunDirs
from scraper.state import AppState


STATUS_OK = {"processed", "ignored"}
STATUS_FAILED = {"onerror", "canceled"}


@dataclass
class InFlight:
    file_path: Path
    upload_id: str
    uploaded_at: float


def _status_value(upload: Upload) -> str:
    status = upload.status
    if status is None:
        return ""
    return str(status).lower()


def _upload_and_wait(
    state: AppState,
    collection_id: str,
    files_in_scraped: list[Path],
    run: RunDirs,
    *,
    concurrency: int,
    poll_interval_s: float = 2.0,
    per_upload_timeout_s: int = 120 * 60,
) -> tuple[int, int]:
    concurrency = max(1, concurrency)

    queue: list[Path] = []
    for f in files_in_scraped:
        if f.exists():
            dest = run.uploading / f.name
            shutil.move(str(f), str(dest))
            queue.append(dest)

    processed = 0
    failed = 0

    uploading_futures: dict[Future, Path] = {}
    inflight: dict[str, InFlight] = {}

    with ThreadPoolExecutor(max_workers=concurrency) as pool:
        while queue or uploading_futures or inflight:
            while queue and (len(inflight) + len(uploading_futures)) < concurrency:
                fp = queue.pop(0)
                fut = pool.submit(_upload_one, state, collection_id, fp)
                uploading_futures[fut] = fp

            if uploading_futures:
                done, _ = wait(uploading_futures.keys(), timeout=0.1, return_when=FIRST_COMPLETED)
                for fut in done:
                    fp = uploading_futures.pop(fut)
                    try:
                        upload_id, initial_status = fut.result()
                        inflight[upload_id] = InFlight(file_path=fp, upload_id=upload_id, uploaded_at=time.time())
                        print(f"Uploaded {fp.name}: id={upload_id} status={initial_status}")
                    except Exception as e:
                        print(f"Upload failed for {fp.name}: {e}")
                        if fp.exists():
                            shutil.move(str(fp), str(run.failed / fp.name))
                        failed += 1

            if inflight:
                ids = list(inflight.keys())
                statuses = _poll_statuses(state, collection_id, ids)
                now = time.time()

                completed: list[str] = []
                for uid, info in inflight.items():
                    st = statuses.get(uid, "unknown")

                    if now - info.uploaded_at > per_upload_timeout_s:
                        print(f"Timeout waiting: id={uid} file={info.file_path.name} last={st}")
                        if info.file_path.exists():
                            shutil.move(str(info.file_path), str(run.failed / info.file_path.name))
                        failed += 1
                        completed.append(uid)
                        continue

                    if st in STATUS_OK:
                        if info.file_path.exists():
                            shutil.move(str(info.file_path), str(run.uploaded / info.file_path.name))
                        processed += 1
                        completed.append(uid)
                        continue

                    if st in STATUS_FAILED:
                        print(f"Status={st}: id={uid} file={info.file_path.name}")
                        if info.file_path.exists():
                            shutil.move(str(info.file_path), str(run.failed / info.file_path.name))
                        failed += 1
                        completed.append(uid)
                        continue

                for uid in completed:
                    inflight.pop(uid, None)

            if inflight and not uploading_futures:
                time.sleep(poll_interval_s)

    return processed, failed


def _upload_one(state: AppState, collection_id: str, fp: Path) -> tuple[str, str]:
    upload = state.aprilflow.upload.upload_file(collection_id, fp)
    upload_id = upload.id
    status = _status_value(upload)
    if not upload_id:
        raise RuntimeError("Upload response missing id")
    return upload_id, status


def _poll_statuses(state: AppState, collection_id: str, ids: list[str]) -> dict[str, str]:
    if not ids:
        return {}
    uploads = state.aprilflow.upload.batch(collection_id, ids)
    out: dict[str, str] = {}
    for upload in uploads:
        if upload.id:
            out[upload.id] = _status_value(upload)
    return out


def upload(state: AppState, run_dir: Path, into: str | None, concurrency: int = 2) -> int:
    run_dir = run_dir.expanduser().resolve()

    scraped = run_dir / "scraped"
    uploading = run_dir / "uploading"
    uploaded = run_dir / "uploaded"
    failed = run_dir / "failed"

    if not run_dir.exists():
        print(f"Run directory not found: {run_dir}")
        return 2

    collection_id = into or state.current_collection_id
    if not collection_id:
        print("No target collection. Use --into <id> or: use <id>")
        return 2

    try:
        _get_collection(state, collection_id)
    except Exception:
        print(f"Unknown collection id: {collection_id}")
        return 2

    if concurrency <= 0:
        print("--concurrency must be a positive integer")
        return 2

    for p in (scraped, uploading, uploaded, failed):
        p.mkdir(parents=True, exist_ok=True)

    for fp in uploading.glob("*"):
        if fp.is_file():
            shutil.move(str(fp), str(scraped / fp.name))

    for fp in failed.glob("*"):
        if fp.is_file():
            shutil.move(str(fp), str(scraped / fp.name))

    files_to_upload = [p for p in scraped.glob("*") if p.is_file()]
    if not files_to_upload:
        print(f"Nothing to upload in: {run_dir}")
        return 0

    run = RunDirs(root=run_dir, scraped=scraped, uploading=uploading, uploaded=uploaded, failed=failed)

    processed, fail_count = _upload_and_wait(
        state,
        collection_id,
        files_to_upload,
        run,
        concurrency=max(1, concurrency),
    )

    print(f"Upload done. processed={processed} failed={fail_count} run={run.root}")
    return 0 if fail_count == 0 else 1
