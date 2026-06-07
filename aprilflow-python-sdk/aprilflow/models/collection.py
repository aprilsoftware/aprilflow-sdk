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
from datetime import datetime
from enum import Enum
from pydantic import Field
from .base import AprilFlowModel

class CollectionStatus(str, Enum):
    CREATED = "Created"
    DELETED = "Deleted"
    ERASING = "Erasing"

class CollectionVisibility(str, Enum):
    PRIVATE = "Private"
    PUBLIC = "Public"
    RESTRICTED = "Restricted"
    USER = "User"

class Collection(AprilFlowModel):
    id: str | None = None
    tenant_id: str | None = Field(None, alias="tenantId")
    creation_date: datetime | None = Field(None, alias="creationDate")
    status: CollectionStatus | None = None
    status_date: datetime | None = Field(None, alias="statusDate")
    name: str | None = None
    description: str | None = None
    visibility: CollectionVisibility | None = None
    code: str | None = None
    user_id: str | None = Field(None, alias="userId")

class CollectionListRequest(AprilFlowModel):
    search: str | None = None
    first_result: int | None = Field(None, alias="firstResult")
    max_result: int | None = Field(None, alias="maxResult")

class CreateCollectionRequest(AprilFlowModel):
    name: str
    description: str | None = None
    visibility: CollectionVisibility | None = None

class UpdateCollectionRequest(AprilFlowModel):
    collection_id: str = Field(alias="collectionId")
    name: str | None = None
    description: str | None = None

class SetCollectionVisibilityRequest(AprilFlowModel):
    collection_id: str = Field(alias="collectionId")
    visibility: CollectionVisibility
    policy_ids: list[str] | None = Field(None, alias="policyIds")
