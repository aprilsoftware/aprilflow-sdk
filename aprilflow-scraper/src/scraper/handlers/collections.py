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

from aprilflow.models.collection import Collection

from scraper.state import AppState


def _get_collection(state: AppState, collection_id: str) -> Collection:
    response = state.aprilflow.http.request(
        "GET",
        f"/collection/v1/pub/collections/{collection_id}",
    )
    return Collection.model_validate(response.json())


def list_collections(state: AppState) -> int:
    items = state.aprilflow.collection.list_by_permissions()

    if not items:
        print("No collections available for this user.")
        return 0

    for c in items:
        cid = c.id or ""
        name = c.name or ""
        marker = " *" if cid == state.current_collection_id else ""
        print(f"{cid}\t{name}{marker}")
    return 0


def use_collection(state: AppState, collection_id: str) -> int:
    try:
        _get_collection(state, collection_id)
    except Exception:
        print(f"Unknown collection id: {collection_id}")
        return 2

    state.current_collection_id = collection_id
    print(f"Using collection: {collection_id}")
    return 0


def get_collection(state: AppState, collection_id: str) -> int:
    try:
        c = _get_collection(state, collection_id)
    except Exception:
        print(f"Unknown collection id: {collection_id}")
        return 2

    print(f"id:         {c.id}")
    print(f"name:       {c.name}")
    print(f"tenantId:   {c.tenant_id}")
    print(f"userId:     {c.user_id}")
    print(f"status:     {c.status}")
    print(f"visibility: {c.visibility}")
    print(f"created:    {c.creation_date}")
    return 0
