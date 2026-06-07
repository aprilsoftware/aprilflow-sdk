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

import threading

import httpx

from aprilflow.http import APIClient
from aprilflow.models.notification import Notification
from aprilflow.models.prompt import Prompt, PromptStatus
from aprilflow.models.upload import Upload, UploadStatus, UploadWaitOptions
from aprilflow.resources.notification import NotificationResource, NotificationSubscription
from aprilflow.resources.prompt import SessionResource
from aprilflow.resources.upload import UploadResource


class ImmediateNotificationResource:
    def __init__(self, notification: Notification):
        self.notification = notification
        self.object_types = None

    def listen(self, object_types=None, listener=None, error_listener=None, **kwargs):
        self.object_types = object_types
        listener(self.notification)
        event = threading.Event()
        thread = threading.Thread(target=lambda: None)
        thread.start(); thread.join()
        return NotificationSubscription(event, thread)


def test_notification_path_filters_object_types(api_client: APIClient) -> None:
    resource = NotificationResource(api_client)

    assert resource.path(["collection.upload", "prompt"]) == "/message/v1/pub/notifications?objectType=collection.upload&objectType=prompt"


def test_sse_parser_dispatches_notification_events(api_client: APIClient) -> None:
    resource = NotificationResource(api_client)
    received: list[Notification] = []
    errors: list[Exception] = []
    response = httpx.Response(
        200,
        content=b'event: ignored\ndata: {"action":"ignored"}\n\nevent: notification\ndata: {"action":"upload.process","objectType":"collection.upload","object":{"id":"u1"}}\n\n',
        request=httpx.Request("GET", "https://api.test/message/v1/pub/notifications"),
    )

    resource._consume_events(response, received.append, errors.append, threading.Event())

    assert not errors
    assert len(received) == 1
    assert received[0].action == "upload.process"
    assert received[0].object_type == "collection.upload"
    assert received[0].object == {"id": "u1"}


def test_upload_wait_uses_upload_notifications() -> None:
    notification = ImmediateNotificationResource(Notification(
        action="upload.process",
        object_type="collection.upload",
        object={"id": "u1", "collectionId": "c1", "status": "Processed"},
    ))
    resource = UploadResource(http=None, notification=notification)  # type: ignore[arg-type]
    upload = Upload(id="u1", collection_id="c1", status=UploadStatus.PROCESSING)

    result = resource.wait_for_upload(upload, UploadWaitOptions(timeout=0.1))

    assert result.status == UploadStatus.PROCESSED
    assert notification.object_types == ["collection.upload"]


def test_session_wait_uses_prompt_notifications() -> None:
    notification = ImmediateNotificationResource(Notification(
        action="prompt.process",
        object_type="prompt",
        object={"id": "p1", "status": "Completed", "output": "done"},
    ))
    resource = SessionResource(http=None, notification=notification)  # type: ignore[arg-type]
    prompt = Prompt(id="p1", status=PromptStatus.THINKING)

    result = resource.wait_for_completion(prompt)

    assert result.status == PromptStatus.COMPLETED
    assert result.output == "done"
    assert notification.object_types == ["prompt"]
