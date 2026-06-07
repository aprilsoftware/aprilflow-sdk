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
from ..models.collection import *

class CollectionResource:
    def __init__(self, http: APIClient): self._http = http

    def list_by_permissions(self) -> list[Collection]:
        return TypeAdapter(list[Collection]).validate_python(self._http.request("GET", "/collection/v1/pub/collections/by-permissions").json())

    def count(self, request: CollectionListRequest | None = None, **kwargs) -> int:
        req = request or CollectionListRequest(**kwargs)
        return int(self._http.request("GET", "/collection/v1/pub/collections/count", params=req.to_api_json()).text)

    def list(self, request: CollectionListRequest | None = None, **kwargs) -> list[Collection]:
        req = request or CollectionListRequest(**kwargs)
        return TypeAdapter(list[Collection]).validate_python(self._http.request("GET", "/collection/v1/pub/collections", params=req.to_api_json()).json())

    def count_deleted(self) -> int:
        return int(self._http.request("GET", "/collection/v1/pub/collections/deleted/count").text)

    def list_deleted(self, first_result: int | None = None, max_result: int | None = None) -> list[Collection]:
        return TypeAdapter(list[Collection]).validate_python(self._http.request("GET", "/collection/v1/pub/collections/deleted", params={"firstResult": first_result, "maxResult": max_result}).json())

    def restore(self, collection_ids: list[str]) -> list[Collection]:
        return TypeAdapter(list[Collection]).validate_python(self._http.request("PUT", "/collection/v1/pub/collections/deleted/restore", json=collection_ids).json())

    def create(self, request: CreateCollectionRequest | None = None, **kwargs) -> Collection:
        req = request or CreateCollectionRequest(**kwargs)
        return Collection.model_validate(self._http.request("POST", "/collection/v1/pub/collections", json=req.to_api_json()).json())

    def update(self, request: UpdateCollectionRequest | None = None, **kwargs) -> Collection:
        req = request or UpdateCollectionRequest(**kwargs)
        return Collection.model_validate(self._http.request("PUT", f"/collection/v1/pub/collections/{segment(req.collection_id)}", json=req.to_api_json()).json())

    def list_policy_ids(self, collection_id: str) -> list[str]:
        return TypeAdapter(list[str]).validate_python(self._http.request("GET", f"/collection/v1/pub/collections/{segment(collection_id)}/visibility").json())

    def set_visibility(self, request: SetCollectionVisibilityRequest | None = None, **kwargs) -> Collection:
        req = request or SetCollectionVisibilityRequest(**kwargs)
        return Collection.model_validate(self._http.request("PUT", f"/collection/v1/pub/collections/{segment(req.collection_id)}/visibility", json=req.to_api_json()).json())

    def delete(self, collection_id: str) -> None:
        self._http.request("DELETE", f"/collection/v1/pub/collections/{segment(collection_id)}")

    def get_user_prompt_collection(self) -> Collection | None:
        r = self._http.request("GET", "/collection/v1/pub/collections/user/prompt")
        return None if r.status_code == 204 or not r.text.strip() else Collection.model_validate(r.json())

    def create_user_prompt_collection(self) -> Collection:
        return Collection.model_validate(self._http.request("POST", "/collection/v1/pub/collections/user/prompt").json())
