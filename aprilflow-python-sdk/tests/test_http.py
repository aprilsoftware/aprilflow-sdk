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

import httpx
import pytest

from aprilflow.exceptions import AprilFlowHTTPError
from aprilflow.http import APIClient, clean_params, segment


class StaticTokenProvider:
    def access_token(self) -> str:
        return "access-token"

    def invalidate(self) -> None:
        pass


def test_segment_url_encodes_path_values() -> None:
    assert segment("folder/a b+c") == "folder%2Fa%20b%2Bc"


def test_clean_params_drops_none_and_serializes_enum_like_values() -> None:
    class EnumLike:
        value = "Serialized"

    assert clean_params({"a": None, "b": 1, "c": [EnumLike(), None, "x"]}) == {
        "b": 1,
        "c": ["Serialized", "x"],
    }


def test_request_adds_bearer_token_and_query_params() -> None:
    captured: dict[str, object] = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["authorization"] = request.headers.get("authorization")
        captured["url"] = str(request.url)
        return httpx.Response(200, json={"ok": True})

    client = APIClient("https://api.test", token_provider=StaticTokenProvider())
    client._client = httpx.Client(base_url="https://api.test", transport=httpx.MockTransport(handler))

    response = client.request("GET", "/resource", params={"firstResult": 0, "maxResult": None})

    assert response.json() == {"ok": True}
    assert captured["authorization"] == "Bearer access-token"
    assert captured["url"] == "https://api.test/resource?firstResult=0"


def test_request_raises_aprilflow_http_error_on_non_2xx() -> None:
    client = APIClient("https://api.test", token_provider=None)
    client._client = httpx.Client(
        base_url="https://api.test",
        transport=httpx.MockTransport(lambda request: httpx.Response(404, text="missing")),
    )

    with pytest.raises(AprilFlowHTTPError) as exc:
        client.request("GET", "/missing")

    assert exc.value.status_code == 404
    assert exc.value.response_body == "missing"
