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

import pytest

from aprilflow import CollectionVisibility

pytestmark = pytest.mark.integration


def test_collection_lifecycle(client, unique_name):
    collection = client.collection.create(
        name=unique_name,
        description="Collection created from the Python SDK integration tests",
        visibility=CollectionVisibility.PRIVATE,
    )
    try:
        collections = client.collection.list(search=unique_name, first_result=0, max_result=10)
        assert any(item.id == collection.id for item in collections)
        assert client.collection.count(search=unique_name) >= 1
        assert isinstance(client.collection.list_by_permissions(), list)

        updated = client.collection.update(
            collection_id=collection.id,
            name=f"{unique_name}-updated",
            description="Updated from the Python SDK integration tests",
        )
        assert updated.name == f"{unique_name}-updated"

        public_collection = client.collection.set_visibility(
            collection_id=collection.id,
            visibility=CollectionVisibility.PUBLIC,
            policy_ids=[],
        )
        assert public_collection.visibility == CollectionVisibility.PUBLIC

        assert isinstance(client.collection.list_policy_ids(collection.id), list)

        client.collection.delete(collection.id)
        assert client.collection.count_deleted() >= 1
        deleted = client.collection.list_deleted(0, 50)
        assert isinstance(deleted, list)
        restored = client.collection.restore([collection.id])
        assert isinstance(restored, list)
    finally:
        try:
            client.collection.delete(collection.id)
        except Exception:
            pass


def test_user_prompt_collection(client):
    existing = client.collection.get_user_prompt_collection()
    created = client.collection.create_user_prompt_collection() if existing is None else existing
    assert created is not None
    fetched = client.collection.get_user_prompt_collection()
    assert fetched is not None
