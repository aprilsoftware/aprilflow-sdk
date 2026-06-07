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

import base64
import json
import time
from dataclasses import dataclass, field
from typing import Any

import httpx
import pytest

from aprilflow.http import APIClient


def make_jwt(exp_offset_seconds: int = 3600, **claims: Any) -> str:
    """Create an unsigned JWT suitable for token parsing tests."""
    header = {"alg": "none", "typ": "JWT"}
    payload = {"exp": int(time.time()) + exp_offset_seconds, **claims}

    def encode(part: dict[str, Any]) -> str:
        raw = json.dumps(part, separators=(",", ":")).encode("utf-8")
        return base64.urlsafe_b64encode(raw).decode("ascii").rstrip("=")

    return f"{encode(header)}.{encode(payload)}."


@dataclass
class RecordedRequest:
    method: str
    path: str
    kwargs: dict[str, Any] = field(default_factory=dict)


class FakeHTTP:
    """Small fake APIClient replacement used by resource unit tests."""

    def __init__(self, responses: list[httpx.Response] | None = None):
        self.responses = responses or [httpx.Response(200, json={})]
        self.requests: list[RecordedRequest] = []

    def request(self, method: str, path: str, **kwargs: Any) -> httpx.Response:
        self.requests.append(RecordedRequest(method=method, path=path, kwargs=kwargs))
        response = self.responses.pop(0)
        response.request = httpx.Request(method, f"https://api.test{path}")
        return response


@pytest.fixture
def api_client() -> APIClient:
    return APIClient("https://api.test", token_provider=None)
