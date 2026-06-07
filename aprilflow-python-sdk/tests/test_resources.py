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

from aprilflow.models.collection import CollectionVisibility
from aprilflow.models.document import DocumentId
from aprilflow.resources.collection import CollectionResource
from aprilflow.resources.document import DocumentResource
from aprilflow.resources.prompt import PromptResource
from aprilflow.resources.upload import UploadResource
from tests.conftest import FakeHTTP


def test_collection_create_posts_expected_payload() -> None:
    fake = FakeHTTP([
        httpx.Response(200, json={"id": "col_123", "name": "Knowledge", "visibility": "Private"})
    ])
    resource = CollectionResource(fake)

    collection = resource.create(name="Knowledge", visibility=CollectionVisibility.PRIVATE)

    assert collection.id == "col_123"
    assert fake.requests[0].method == "POST"
    assert fake.requests[0].path == "/collection/v1/pub/collections"
    assert fake.requests[0].kwargs["json"] == {"name": "Knowledge", "visibility": "Private"}


def test_collection_list_passes_pagination_query_params() -> None:
    fake = FakeHTTP([httpx.Response(200, json=[])])
    resource = CollectionResource(fake)

    assert resource.list(search="abc", first_result=5, max_result=10) == []

    assert fake.requests[0].method == "GET"
    assert fake.requests[0].path == "/collection/v1/pub/collections"
    assert fake.requests[0].kwargs["params"] == {
        "search": "abc",
        "firstResult": 5,
        "maxResult": 10,
    }


def test_upload_batch_and_delete_use_encoded_collection_and_upload_ids() -> None:
    fake = FakeHTTP([
        httpx.Response(200, json=[]),
        httpx.Response(204),
    ])
    resource = UploadResource(fake)

    resource.batch("collection/id", ["upl_1"])
    resource.delete("collection/id", "upload/id", delete_document=True)

    assert fake.requests[0].path == "/collection/v1/pub/collection%2Fid/uploads/ids"
    assert fake.requests[0].kwargs["json"] == ["upl_1"]
    assert fake.requests[1].method == "DELETE"
    assert fake.requests[1].path == "/collection/v1/pub/collection%2Fid/uploads/upload%2Fid"
    assert fake.requests[1].kwargs["params"] == {"deleteDocument": True}


def test_document_batch_items_serializes_document_ids() -> None:
    fake = FakeHTTP([httpx.Response(200, json=[])])
    resource = DocumentResource(fake)

    resource.batch_items([DocumentId(collection_id="col_123", document_id="doc_123")])

    assert fake.requests[0].method == "POST"
    assert fake.requests[0].path == "/collection/v1/pub/documents/ids"
    assert fake.requests[0].kwargs["json"] == [{"collectionId": "col_123", "documentId": "doc_123"}]


def test_session_create_posts_prompt_request() -> None:
    fake = FakeHTTP([
        httpx.Response(
            200,
            json={
                "session": {"id": "ses_123", "title": "First"},
                "prompt": {"id": "pro_123", "status": "Created", "sessionId": "ses_123"},
            },
        )
    ])
    prompt = PromptResource(fake)

    result = prompt.session.create(text="Hello", collection_ids=["col_123"])

    assert result.session.id == "ses_123"
    assert result.prompt.session_id == "ses_123"
    assert fake.requests[0].method == "POST"
    assert fake.requests[0].path == "/prompt/v1/pub/sessions"
    assert fake.requests[0].kwargs["json"] == {"text": "Hello", "collectionIds": ["col_123"], "uploadIds": []}
