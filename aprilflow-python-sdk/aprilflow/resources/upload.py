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
import mimetypes
import threading

from pydantic import TypeAdapter

from ..exceptions import AprilFlowError
from ..http import APIClient, segment
from ..models.notification import Notification
from ..models.upload import *
from .notification import NotificationResource, NotificationSubscription


class UploadResource:
    UPLOAD_ACTION = "upload.process"
    UPLOAD_OBJECT_TYPE = "collection.upload"

    def __init__(self, http: APIClient, notification: NotificationResource | None = None):
        self._http = http
        self._notification = notification

    def list(self, request: UploadListRequest | None = None, **kwargs) -> list[Upload]:
        req = request or UploadListRequest(**kwargs)
        data = req.to_api_json(); collection_id = data.pop("collectionId")
        return TypeAdapter(list[Upload]).validate_python(self._http.request("GET", f"/collection/v1/pub/{segment(collection_id)}/uploads", params=data).json())

    def count(self, request: UploadListRequest | None = None, **kwargs) -> int:
        req = request or UploadListRequest(**kwargs)
        data = req.to_api_json(); collection_id = data.pop("collectionId")
        return int(self._http.request("GET", f"/collection/v1/pub/{segment(collection_id)}/uploads/count", params=data).text)

    def batch(self, collection_id: str, upload_ids: list[str]) -> list[Upload]:
        return TypeAdapter(list[Upload]).validate_python(self._http.request("POST", f"/collection/v1/pub/{segment(collection_id)}/uploads/ids", json=upload_ids).json())

    def upload_file(self, collection_id: str, file: str | Path, file_name: str | None = None, content_type: str | None = None) -> Upload:
        path = Path(file); file_name = file_name or path.name; content_type = content_type or mimetypes.guess_type(file_name)[0] or "application/octet-stream"
        with path.open("rb") as fh:
            files = {"file": (file_name, fh, content_type)}
            return Upload.model_validate(self._http.request("POST", f"/collection/v1/pub/{segment(collection_id)}/uploads", files=files).json())

    def upload_bytes(self, collection_id: str, data: bytes, file_name: str, content_type: str | None = None) -> Upload:
        files = {"file": (file_name, data, content_type or "application/octet-stream")}
        return Upload.model_validate(self._http.request("POST", f"/collection/v1/pub/{segment(collection_id)}/uploads", files=files).json())

    def upload_stream(self, collection_id: str, stream, file_name: str, content_type: str | None = None) -> Upload:
        files = {"file": (file_name, stream, content_type or "application/octet-stream")}
        return Upload.model_validate(self._http.request("POST", f"/collection/v1/pub/{segment(collection_id)}/uploads", files=files).json())

    def upload_file_and_wait(self, collection_id: str, file: str | Path, file_name: str | None = None, content_type: str | None = None, wait_options: UploadWaitOptions | None = None) -> Upload:
        upload = self.upload_file(collection_id, file, file_name=file_name, content_type=content_type)
        return self.wait_for_upload(upload, wait_options)

    def upload_bytes_and_wait(self, collection_id: str, data: bytes, file_name: str, content_type: str | None = None, wait_options: UploadWaitOptions | None = None) -> Upload:
        upload = self.upload_bytes(collection_id, data, file_name=file_name, content_type=content_type)
        return self.wait_for_upload(upload, wait_options)

    def upload_stream_and_wait(self, collection_id: str, stream, file_name: str, content_type: str | None = None, wait_options: UploadWaitOptions | None = None) -> Upload:
        upload = self.upload_stream(collection_id, stream, file_name=file_name, content_type=content_type)
        return self.wait_for_upload(upload, wait_options)

    def watch(self, collection_id: str, upload_id: str, listener, error_listener=None) -> NotificationSubscription:
        if self._notification is None:
            raise AprilFlowError("Notification API is required to watch uploads")

        def on_notification(notification: Notification) -> None:
            upload = self._to_upload(notification)
            if upload is None:
                return
            if upload.collection_id != collection_id or upload.id != upload_id:
                return
            listener(notification)

        return self._notification.listen([self.UPLOAD_OBJECT_TYPE], on_notification, error_listener)

    def wait_for_upload(self, upload: Upload, wait_options: UploadWaitOptions | None = None) -> Upload:
        options = wait_options or UploadWaitOptions()
        if upload.status in options.terminal_statuses:
            return upload
        if upload.collection_id is None or upload.id is None:
            raise AprilFlowError("Upload must include collection_id and id before it can be watched")

        completed = threading.Event()
        result: dict[str, Upload | BaseException] = {}

        def on_notification(notification: Notification) -> None:
            notified_upload = self._to_upload(notification)
            if notified_upload is not None and notified_upload.status in options.terminal_statuses:
                result["upload"] = notified_upload
                completed.set()

        def on_error(error: Exception) -> None:
            result["error"] = error
            completed.set()

        subscription = self.watch(upload.collection_id, upload.id, on_notification, on_error)
        try:
            if not completed.wait(options.timeout):
                raise AprilFlowError(f"Timed out waiting for upload {upload.id} to complete")
            if "error" in result:
                raise AprilFlowError(f"Error while waiting for upload {upload.id} to complete") from result["error"]  # type: ignore[misc]
            if "upload" not in result:
                raise AprilFlowError(f"Upload {upload.id} completed without an upload payload")
            return result["upload"]  # type: ignore[return-value]
        finally:
            subscription.close()

    def delete(self, collection_id: str, upload_id: str, delete_document: bool = False) -> None:
        self._http.request("DELETE", f"/collection/v1/pub/{segment(collection_id)}/uploads/{segment(upload_id)}", params={"deleteDocument": delete_document})

    def delete_document(self, collection_id: str, upload_id: str) -> None:
        self._http.request("DELETE", f"/collection/v1/pub/{segment(collection_id)}/uploads/{segment(upload_id)}/document")

    def cancel(self, collection_id: str, upload_id: str) -> Upload:
        return Upload.model_validate(self._http.request("PUT", f"/collection/v1/pub/{segment(collection_id)}/uploads/{segment(upload_id)}/process/cancel").json())

    def restart(self, collection_id: str, upload_id: str) -> Upload:
        return Upload.model_validate(self._http.request("PUT", f"/collection/v1/pub/{segment(collection_id)}/uploads/{segment(upload_id)}/process/restart").json())

    def _to_upload(self, notification: Notification) -> Upload | None:
        if notification.action != self.UPLOAD_ACTION or notification.object_type != self.UPLOAD_OBJECT_TYPE or notification.object is None:
            return None
        return Upload.model_validate(notification.object)
