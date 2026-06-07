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

from pydantic import TypeAdapter

from ..exceptions import AprilFlowError
from ..http import APIClient, segment
from ..models.notification import Notification
from ..models.prompt import *
from .notification import NotificationResource


class SessionResource:
    PROMPT_ACTION = "prompt.process"
    PROMPT_OBJECT_TYPE = "prompt"

    def __init__(self, http: APIClient, notification: NotificationResource | None = None):
        self._http = http
        self._notification = notification

    def create(self, request: PromptRequest | None = None, **kwargs) -> CreateSessionResult:
        req = request or PromptRequest(**kwargs)
        return CreateSessionResult.model_validate(self._http.request("POST", "/prompt/v1/pub/sessions", json=req.to_api_json()).json())

    def create_and_wait(self, request: PromptRequest | None = None, wait_options: PromptWaitOptions | None = None, **kwargs) -> CreateSessionResult:
        result = self.create(request, **kwargs)
        prompt = self.wait_for_completion(result.prompt, wait_options)
        return CreateSessionResult(session=result.session, prompt=prompt)

    def update(self, session_id: str, request: PromptRequest | None = None, **kwargs) -> Prompt:
        req = request or PromptRequest(**kwargs)
        return Prompt.model_validate(self._http.request("PUT", f"/prompt/v1/pub/sessions/{segment(session_id)}", json=req.to_api_json()).json())

    def update_and_wait(self, session_id: str, request: PromptRequest | None = None, wait_options: PromptWaitOptions | None = None, **kwargs) -> Prompt:
        prompt = self.update(session_id, request, **kwargs)
        return self.wait_for_completion(prompt, wait_options)

    def update_title(self, session_id: str, title: str) -> Session:
        return Session.model_validate(self._http.request("PUT", f"/prompt/v1/pub/sessions/{segment(session_id)}/title", content=title, headers={"Content-Type": "text/plain"}).json())

    def duplicate(self, session_id: str, title: str) -> Session:
        return Session.model_validate(self._http.request("POST", f"/prompt/v1/pub/sessions/{segment(session_id)}/duplicate", content=title, headers={"Content-Type": "text/plain"}).json())

    def count(self) -> int:
        return int(self._http.request("GET", "/prompt/v1/pub/sessions/count").text)

    def list(self, request: SessionListRequest | None = None, **kwargs) -> list[Session]:
        req = request or SessionListRequest(**kwargs)
        return TypeAdapter(list[Session]).validate_python(self._http.request("GET", "/prompt/v1/pub/sessions", params=req.to_api_json()).json())

    def delete(self, session_id: str) -> None:
        self._http.request("DELETE", f"/prompt/v1/pub/sessions/{segment(session_id)}")

    def wait_for_completion(self, prompt: Prompt, wait_options: PromptWaitOptions | None = None) -> Prompt:
        options = wait_options or PromptWaitOptions()
        if prompt.status in options.terminal_statuses:
            return prompt
        if prompt.id is None:
            raise AprilFlowError("Prompt must include id before it can be watched")
        if self._notification is None:
            raise AprilFlowError("Notification API is required to wait for prompts")

        completed = threading.Event()
        result: dict[str, Prompt | BaseException] = {}

        def on_notification(notification: Notification) -> None:
            notified_prompt = self._to_prompt(notification)
            if notified_prompt is None:
                return
            if notified_prompt.id != prompt.id:
                return
            if notified_prompt.status in options.terminal_statuses:
                result["prompt"] = notified_prompt
                completed.set()

        def on_error(error: Exception) -> None:
            result["error"] = error
            completed.set()

        subscription = self._notification.listen([self.PROMPT_OBJECT_TYPE], on_notification, on_error)
        try:
            if not completed.wait(options.timeout):
                raise AprilFlowError(f"Timed out waiting for prompt {prompt.id} to complete")
            if "error" in result:
                raise AprilFlowError(f"Error while waiting for prompt {prompt.id} to complete") from result["error"]  # type: ignore[misc]
            if "prompt" not in result:
                raise AprilFlowError(f"Prompt {prompt.id} completed without a prompt payload")
            return result["prompt"]  # type: ignore[return-value]
        finally:
            subscription.close()

    def _to_prompt(self, notification: Notification) -> Prompt | None:
        if notification.action != self.PROMPT_ACTION or notification.object_type != self.PROMPT_OBJECT_TYPE or notification.object is None:
            return None
        return Prompt.model_validate(notification.object)


class PromptResource:
    def __init__(self, http: APIClient, notification: NotificationResource | None = None):
        self._http = http
        self.session = SessionResource(http, notification)

    def built_with(self) -> str:
        return self._http.request("GET", "/prompt/v1/pub/system/built-with").text

    def list_user_collections(self) -> list[UserCollection]:
        return TypeAdapter(list[UserCollection]).validate_python(self._http.request("GET", "/prompt/v1/pub/users/collections").json())

    def set_user_collections(self, collection_ids: list[str]) -> None:
        self._http.request("PUT", "/prompt/v1/pub/users/collections", json=collection_ids)

    def list_by_session(self, session_id: str) -> list[Prompt]:
        return TypeAdapter(list[Prompt]).validate_python(self._http.request("GET", f"/prompt/v1/pub/prompts/sessions/{segment(session_id)}").json())

    def restart(self, prompt_id: str) -> Prompt:
        return Prompt.model_validate(self._http.request("PUT", f"/prompt/v1/pub/prompts/{segment(prompt_id)}/restart").json())

    def interrupt(self, prompt_id: str) -> Prompt:
        return Prompt.model_validate(self._http.request("PUT", f"/prompt/v1/pub/prompts/{segment(prompt_id)}/interrupt").json())

    def delete(self, prompt_id: str) -> None:
        self._http.request("DELETE", f"/prompt/v1/pub/prompts/{segment(prompt_id)}")
