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
from pydantic import TypeAdapter
from ..http import APIClient, segment
from ..models.billing import *

class QuotaResource:
    def __init__(self, http: APIClient): self._http = http
    def count(self) -> int: return int(self._http.request("GET", "/billing/v1/pub/quotas/count").text)
    def list(self, request: QuotaListRequest | None = None, **kwargs) -> list[Quota]:
        req = request or QuotaListRequest(**kwargs)
        return TypeAdapter(list[Quota]).validate_python(self._http.request("GET", "/billing/v1/pub/quotas", params=req.to_api_json()).json())
    def insert(self, quota: Quota) -> Quota:
        return Quota.model_validate(self._http.request("POST", "/billing/v1/pub/quotas", json=quota.to_api_json()).json())
    def update(self, quota: Quota) -> Quota:
        return Quota.model_validate(self._http.request("PUT", f"/billing/v1/pub/quotas/{segment(quota.id)}", json=quota.to_api_json()).json())
    def delete(self, quota_id: str) -> None:
        self._http.request("DELETE", f"/billing/v1/pub/quotas/{segment(quota_id)}")

class QuotaTypeResource:
    def __init__(self, http: APIClient): self._http = http
    def list(self) -> list[QuotaType]:
        return TypeAdapter(list[QuotaType]).validate_python(self._http.request("GET", "/billing/v1/pub/quotas/types").json())

class UsageResource:
    def __init__(self, http: APIClient): self._http = http
    def list(self) -> list[Usage]:
        return TypeAdapter(list[Usage]).validate_python(self._http.request("GET", "/billing/v1/pub/usages").json())

class BillingResource:
    def __init__(self, http: APIClient):
        self.quota = QuotaResource(http); self.quota_type = QuotaTypeResource(http); self.usage = UsageResource(http)
