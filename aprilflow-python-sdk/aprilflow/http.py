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
from typing import Any, Iterable, TypeVar
from urllib.parse import quote
import httpx
from pydantic import TypeAdapter
from .exceptions import AprilFlowHTTPError

T = TypeVar("T")


def segment(value: str) -> str:
    return quote(str(value), safe="")


def clean_params(params: dict[str, Any] | None) -> dict[str, Any] | None:
    if not params:
        return None
    cleaned: dict[str, Any] = {}
    for key, value in params.items():
        if value is None:
            continue
        if isinstance(value, list):
            cleaned[key] = [getattr(v, "value", v) for v in value if v is not None]
        else:
            cleaned[key] = getattr(value, "value", value)
    return cleaned


class APIClient:
    def __init__(self, base_url: str, token_provider: Any | None = None, timeout: float = 60.0):
        self.base_url = base_url.rstrip("/")
        self.token_provider = token_provider
        self._client = httpx.Client(base_url=self.base_url, timeout=timeout)

    def close(self) -> None:
        self._client.close()

    def request(self, method: str, path: str, **kwargs: Any) -> httpx.Response:
        headers = dict(kwargs.pop("headers", {}) or {})
        if self.token_provider is not None:
            headers.setdefault("Authorization", f"Bearer {self.token_provider.access_token()}")
        if "params" in kwargs:
            kwargs["params"] = clean_params(kwargs["params"])
        response = self._client.request(method, path, headers=headers, **kwargs)
        if response.status_code < 200 or response.status_code >= 300:
            raise AprilFlowHTTPError(
                f"April Flow API request failed with status {response.status_code}",
                response.status_code,
                response.text,
            )
        return response


    def stream(self, method: str, path: str, **kwargs: Any):
        headers = dict(kwargs.pop("headers", {}) or {})
        if self.token_provider is not None:
            headers.setdefault("Authorization", f"Bearer {self.token_provider.access_token()}")
        if "params" in kwargs:
            kwargs["params"] = clean_params(kwargs["params"])
        context = self._client.stream(method, path, headers=headers, **kwargs)
        return _CheckedStream(context)

    def get_json(self, method: str, path: str, model: type[T], **kwargs: Any) -> T:
        response = self.request(method, path, **kwargs)
        return model.model_validate(response.json())  # type: ignore[attr-defined]

    def get_list(self, method: str, path: str, model: type[T], **kwargs: Any) -> list[T]:
        response = self.request(method, path, **kwargs)
        return TypeAdapter(list[model]).validate_python(response.json())  # type: ignore[valid-type]


class _CheckedStream:
    def __init__(self, context):
        self._context = context
        self._response = None

    def __enter__(self):
        response = self._context.__enter__()
        self._response = response
        if response.status_code < 200 or response.status_code >= 300:
            body = response.read().decode("utf-8", errors="replace")
            self._context.__exit__(None, None, None)
            raise AprilFlowHTTPError(
                f"April Flow API request failed with status {response.status_code}",
                response.status_code,
                body,
            )
        return response

    def __exit__(self, exc_type, exc, tb):
        return self._context.__exit__(exc_type, exc, tb)
