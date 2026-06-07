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
from ..models.document import *

class DocumentResource:
    def __init__(self, http: APIClient): self._http = http

    def batch_items(self, document_ids: list[DocumentId] | list[dict]) -> list[DocumentItem]:
        payload = [d.to_api_json() if hasattr(d, "to_api_json") else d for d in document_ids]
        return TypeAdapter(list[DocumentItem]).validate_python(self._http.request("POST", "/collection/v1/pub/documents/ids", json=payload).json())

    def search(self, request: DocumentSearchRequest | None = None, **kwargs) -> list[DocumentItem]:
        req = request or DocumentSearchRequest(**kwargs)
        return TypeAdapter(list[DocumentItem]).validate_python(self._http.request("POST", f"/collection/v1/pub/{segment(req.collection_id)}/documents/search", params={"maxResult": req.max_result}, content=req.text, headers={"Content-Type": "application/json"}).json())

    def get_object_as_string(self, collection_id: str, document_id: str) -> str:
        return self._http.request("GET", f"/collection/v1/pub/{segment(collection_id)}/documents/{segment(document_id)}/object").text

    def get_object_as_bytes(self, collection_id: str, document_id: str) -> bytes:
        return self._http.request("GET", f"/collection/v1/pub/{segment(collection_id)}/documents/{segment(document_id)}/object").content

    def get_original_document(self, collection_id: str, document_id: str) -> Document:
        return Document.model_validate(self._http.request("GET", f"/collection/v1/pub/{segment(collection_id)}/documents/{segment(document_id)}/original").json())

    def delete_original_document(self, collection_id: str, document_id: str) -> None:
        self._http.request("DELETE", f"/collection/v1/pub/{segment(collection_id)}/documents/{segment(document_id)}/original")

    def delete(self, collection_id: str, document_id: str) -> None:
        self._http.request("DELETE", f"/collection/v1/pub/{segment(collection_id)}/documents/{segment(document_id)}")
