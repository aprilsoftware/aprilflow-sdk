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

from aprilflow.auth import IdentityToken, UserKeyTokenProvider
from aprilflow.exceptions import AprilFlowError
from tests.conftest import make_jwt


class RawHTTP:
    def __init__(self, tokens: list[str]):
        self.tokens = tokens
        self.calls: list[tuple[str, str, dict]] = []

    def request(self, method: str, path: str, **kwargs):
        self.calls.append((method, path, kwargs))
        return httpx.Response(200, text=self.tokens.pop(0))


def test_identity_token_parse_reads_payload_and_expiration() -> None:
    token = make_jwt(exp_offset_seconds=3600, sub="user_123")

    parsed = IdentityToken.parse(token)

    assert parsed.encoded_token == token
    assert parsed.payload["sub"] == "user_123"
    assert not parsed.is_expired()


def test_identity_token_parse_rejects_invalid_token() -> None:
    with pytest.raises(AprilFlowError):
        IdentityToken.parse("not-a-jwt")


def test_user_key_provider_fetches_access_and_refresh_tokens_once() -> None:
    access = make_jwt(exp_offset_seconds=3600)
    refresh = make_jwt(exp_offset_seconds=7200)
    raw = RawHTTP([access, refresh])
    provider = UserKeyTokenProvider(raw, user_key="user-key")

    assert provider.access_token() == access
    assert provider.access_token() == access

    assert [call[1] for call in raw.calls] == [
        "/identity/v1/pub/tenants/auth/token/key",
        "/identity/v1/pub/tenants/auth/token/refresh",
    ]
    assert raw.calls[0][2]["headers"] == {"Authorization": "Bearer user-key"}


def test_user_key_provider_invalidates_cached_tokens() -> None:
    first_access = make_jwt(exp_offset_seconds=3600, version=1)
    first_refresh = make_jwt(exp_offset_seconds=7200, version=1)
    second_access = make_jwt(exp_offset_seconds=3600, version=2)
    second_refresh = make_jwt(exp_offset_seconds=7200, version=2)
    raw = RawHTTP([first_access, first_refresh, second_access, second_refresh])
    provider = UserKeyTokenProvider(raw, user_key="user-key")

    assert provider.access_token() == first_access
    provider.invalidate()
    assert provider.access_token() == second_access

    assert len(raw.calls) == 4
