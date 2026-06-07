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
from .http import APIClient
from .auth import UserKeyTokenProvider
from .resources import CollectionResource, UploadResource, DocumentResource, PromptResource, IdentityResource, BillingResource, MessageResource, NotificationResource

class AprilFlowClient:
    def __init__(self, base_url: str, user_key: str, timeout: float = 60.0):
        raw_http = APIClient(base_url=base_url, token_provider=None, timeout=timeout)
        token_provider = UserKeyTokenProvider(raw_http, user_key)
        self.http = APIClient(base_url=base_url, token_provider=token_provider, timeout=timeout)
        self.collection = CollectionResource(self.http)
        self.notification = NotificationResource(self.http)
        self.upload = UploadResource(self.http, self.notification)
        self.document = DocumentResource(self.http)
        self.prompt = PromptResource(self.http, self.notification)
        self.identity = IdentityResource(self.http)
        self.billing = BillingResource(self.http)
        self.message = MessageResource(self.http)

    @classmethod
    def create(cls, base_url: str, user_key: str, timeout: float = 60.0) -> "AprilFlowClient":
        return cls(base_url=base_url, user_key=user_key, timeout=timeout)

    def close(self) -> None:
        self.http.close()
