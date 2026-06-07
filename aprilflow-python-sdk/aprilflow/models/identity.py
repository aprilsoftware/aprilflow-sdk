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

class TenantStatus(str, Enum):
    CREATED = "Created"
    ACTIVATED = "Activated"
    DELETED = "Deleted"

class TenantRole(str, Enum):
    TENANT_READ = "TenantRead"
    TENANT_UPDATE = "TenantUpdate"
    TENANT_DELETE = "TenantDelete"
    USER_CREATE = "UserCreate"
    USER_READ = "UserRead"
    USER_UPDATE = "UserUpdate"
    USER_DELETE = "UserDelete"
    POLICY_CREATE = "PolicyCreate"
    POLICY_READ = "PolicyRead"
    POLICY_UPDATE = "PolicyUpdate"
    POLICY_DELETE = "PolicyDelete"
    COLLECTION_CREATE = "CollectionCreate"
    COLLECTION_READ = "CollectionRead"
    COLLECTION_UPDATE = "CollectionUpdate"
    COLLECTION_DELETE = "CollectionDelete"
    PROMPT_CREATE = "PromptCreate"
    PROMPT_READ = "PromptRead"
    PROMPT_UPDATE = "PromptUpdate"
    PROMPT_DELETE = "PromptDelete"
    USER_KEY_CREATE = "UserKeyCreate"
    USER_KEY_READ = "UserKeyRead"
    USER_KEY_UPDATE = "UserKeyUpdate"
    USER_KEY_DELETE = "UserKeyDelete"
    QUOTA_CREATE = "QuotaCreate"
    QUOTA_READ = "QuotaRead"
    QUOTA_UPDATE = "QuotaUpdate"
    QUOTA_DELETE = "QuotaDelete"

class Country(AprilFlowModel):
    code: str | None = None
    name: str | None = None

class Tenant(AprilFlowModel):
    id: str | None = None
    registration_date: datetime | None = Field(None, alias="registrationDate")
    status: TenantStatus | None = None
    status_date: datetime | None = Field(None, alias="statusDate")

class Policy(AprilFlowModel):
    id: str | None = None
    name: str | None = None
    description: str | None = None
    tenant_id: str | None = Field(None, alias="tenantId")
    roles: list[TenantRole] = []

class PolicyListRequest(AprilFlowModel):
    search: str | None = None
    first_result: int | None = Field(None, alias="firstResult")
    max_result: int | None = Field(None, alias="maxResult")

class User(AprilFlowModel):
    id: str | None = None
    email: str | None = None
    first_name: str | None = Field(None, alias="firstName")
    last_name: str | None = Field(None, alias="lastName")
    address: str | None = None
    postal_code: str | None = Field(None, alias="postalCode")
    city: str | None = None
    country_code: str | None = Field(None, alias="countryCode")
    phone_number: str | None = Field(None, alias="phoneNumber")
    active: bool | None = None
    enabled: bool | None = None
    totp_enabled: bool | None = Field(None, alias="totpEnabled")
    totp_setup_completed: bool | None = Field(None, alias="totpSetupCompleted")
    tenant_id: str | None = Field(None, alias="tenantId")
    policy_ids: list[str] = Field(default_factory=list, alias="policyIds")

class UserListRequest(AprilFlowModel):
    search: str | None = None
    first_result: int | None = Field(None, alias="firstResult")
    max_result: int | None = Field(None, alias="maxResult")

class UserKey(AprilFlowModel):
    tenant_id: str | None = Field(None, alias="tenantId")
    key_id: str | None = Field(None, alias="keyId")
    user_id: str | None = Field(None, alias="userId")
    creation_date: datetime | None = Field(None, alias="creationDate")
    last_used_date: datetime | None = Field(None, alias="lastUsedDate")
    revoked_date: datetime | None = Field(None, alias="revokedDate")
    usage_count: int | None = Field(None, alias="usageCount")

class UserKeyListRequest(AprilFlowModel):
    first_result: int | None = Field(None, alias="firstResult")
    max_result: int | None = Field(None, alias="maxResult")
