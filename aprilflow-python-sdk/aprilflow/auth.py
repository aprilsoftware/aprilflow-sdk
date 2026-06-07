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
import base64, json, threading, time
from dataclasses import dataclass
from typing import Any, Protocol
from .exceptions import AprilFlowError


class TokenProvider(Protocol):
    def access_token(self) -> str: ...
    def invalidate(self) -> None: ...


@dataclass(frozen=True)
class IdentityToken:
    encoded_token: str
    payload: dict[str, Any]

    @property
    def expiration_time(self) -> float:
        exp = self.payload.get("exp")
        if exp is None:
            raise AprilFlowError("Token does not contain exp claim.")
        return float(exp)

    def is_expired(self, skew_seconds: int = 30) -> bool:
        return time.time() + skew_seconds >= self.expiration_time

    @classmethod
    def parse(cls, token: str) -> "IdentityToken":
        parts = token.split(".")
        if len(parts) != 3:
            raise AprilFlowError("Invalid token.")
        padded = parts[1] + "=" * (-len(parts[1]) % 4)
        payload = json.loads(base64.urlsafe_b64decode(padded).decode("utf-8"))
        return cls(token, payload)


class UserKeyTokenProvider:
    def __init__(self, raw_http_client, user_key: str):
        self._http = raw_http_client
        self._user_key = user_key
        self._lock = threading.RLock()
        self._access: IdentityToken | None = None
        self._refresh: IdentityToken | None = None

    def access_token(self) -> str:
        with self._lock:
            if self._access is None:
                self._access = self._get_access_token()
                self._refresh = self._get_refresh_token()
            if self._access.is_expired():
                if self._refresh is None or self._refresh.is_expired():
                    raise AprilFlowError("Session expired.")
                self._access = self._refresh_access_token()
                if time.time() + 24 * 3600 < self._refresh.expiration_time:
                    self._refresh = self._get_refresh_token()
            return self._access.encoded_token

    def invalidate(self) -> None:
        with self._lock:
            self._access = None
            self._refresh = None

    def _get_access_token(self) -> IdentityToken:
        r = self._http.request("GET", "/identity/v1/pub/tenants/auth/token/key", headers={"Authorization": f"Bearer {self._user_key}"})
        return IdentityToken.parse(r.text)

    def _get_refresh_token(self) -> IdentityToken:
        assert self._access is not None
        r = self._http.request("GET", "/identity/v1/pub/tenants/auth/token/refresh", headers={"Authorization": f"Bearer {self._access.encoded_token}"})
        return IdentityToken.parse(r.text)

    def _refresh_access_token(self) -> IdentityToken:
        assert self._refresh is not None
        r = self._http.request("GET", "/identity/v1/pub/tenants/auth/refresh", headers={"Authorization": f"Bearer {self._refresh.encoded_token}"})
        return IdentityToken.parse(r.text)
