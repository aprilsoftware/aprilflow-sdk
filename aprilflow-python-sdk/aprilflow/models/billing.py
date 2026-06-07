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
from enum import Enum
from pydantic import Field
from .base import AprilFlowModel

class QuotaScope(str, Enum):
    TENANT = "Tenant"

class QuotaTarget(str, Enum):
    TENANT = "Tenant"
    USER = "User"

class Quota(AprilFlowModel):
    id: str | None = None
    tenant_id: str | None = Field(None, alias="tenantId")
    quota_target: QuotaTarget | None = Field(None, alias="quotaTarget")
    quota_scope: QuotaScope | None = Field(None, alias="quotaScope")
    usage_id: str | None = Field(None, alias="usageId")
    quota_type_id: str | None = Field(None, alias="quotaTypeId")
    quota_window: int | None = Field(None, alias="quotaWindow")
    description: str | None = None
    value: int | None = None

class QuotaListRequest(AprilFlowModel):
    first_result: int | None = Field(None, alias="firstResult")
    max_result: int | None = Field(None, alias="maxResult")

class QuotaType(AprilFlowModel):
    id: str | None = None
    code: str | None = None
    name: str | None = None

class Usage(AprilFlowModel):
    id: str | None = None
    code: str | None = None
    name: str | None = None
    quota_type_ids: list[str] = Field(default_factory=list, alias="quotaTypeIds")
