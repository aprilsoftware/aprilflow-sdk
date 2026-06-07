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

from aprilflow.models.collection import Collection, CollectionVisibility, CreateCollectionRequest
from aprilflow.models.upload import UploadListRequest, UploadStatus
from aprilflow.models.prompt import PromptRequest, PromptUploadId


def test_model_accepts_api_aliases_and_exports_api_json() -> None:
    collection = Collection.model_validate(
        {
            "id": "col_123",
            "tenantId": "tenant_123",
            "creationDate": "2026-01-02T03:04:05Z",
            "name": "Contracts",
            "visibility": "Private",
            "ignoredServerField": "ignored",
        }
    )

    assert collection.id == "col_123"
    assert collection.tenant_id == "tenant_123"
    assert collection.visibility == CollectionVisibility.PRIVATE
    assert collection.to_api_json()["tenantId"] == "tenant_123"
    assert "ignoredServerField" not in collection.to_api_json()


def test_request_models_use_pythonic_names_but_emit_java_api_names() -> None:
    request = UploadListRequest(
        collection_id="col_123",
        first_result=10,
        max_result=20,
        statuses=[UploadStatus.UPLOADED, UploadStatus.PROCESSED],
    )

    assert request.to_api_json() == {
        "collectionId": "col_123",
        "firstResult": 10,
        "maxResult": 20,
        "statuses": ["Uploaded", "Processed"],
    }


def test_nested_prompt_upload_ids_are_serialized_with_aliases() -> None:
    request = PromptRequest(
        text="Summarize these files",
        collection_ids=["col_123"],
        upload_ids=[PromptUploadId(collection_id="col_123", upload_id="upl_123")],
    )

    assert request.to_api_json() == {
        "text": "Summarize these files",
        "collectionIds": ["col_123"],
        "uploadIds": [{"collectionId": "col_123", "uploadId": "upl_123"}],
    }


def test_create_collection_request_supports_enum_values() -> None:
    request = CreateCollectionRequest(name="Knowledge", visibility=CollectionVisibility.RESTRICTED)

    assert request.to_api_json() == {"name": "Knowledge", "visibility": "Restricted"}
