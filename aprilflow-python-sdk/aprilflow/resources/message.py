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
from ..http import APIClient
from ..models.message import CreateSupportThreadRequest

class SupportThreadResource:
    def __init__(self, http: APIClient): self._http = http
    def create(self, request: CreateSupportThreadRequest | None = None, **kwargs) -> str:
        req = request or CreateSupportThreadRequest(**kwargs)
        return self._http.request("POST", "/message/v1/pub/threads/support", json=req.to_api_json()).text

class MessageResource:
    def __init__(self, http: APIClient): self.support_thread = SupportThreadResource(http)
