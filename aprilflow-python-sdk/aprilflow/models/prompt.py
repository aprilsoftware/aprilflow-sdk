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

class PromptStatus(str, Enum):
    CREATED = "Created"
    THINKING = "Thinking"
    COMPLETED = "Completed"
    INTERRUPTED = "Interrupted"
    ON_ERROR = "OnError"
    QUOTA_EXCEEDED = "QuotaExceeded"

class PromptHandlerType(str, Enum):
    DIRECT = "Direct"
    REASONER = "Reasoner"

class PromptUploadId(AprilFlowModel):
    collection_id: str = Field(alias="collectionId")
    upload_id: str = Field(alias="uploadId")

class DocumentChunk(AprilFlowModel):
    start_line: int | None = Field(None, alias="startLine")
    end_line: int | None = Field(None, alias="endLine")
    score: float | None = None

class RetrievedDocument(AprilFlowModel):
    collection_id: str | None = Field(None, alias="collectionId")
    document_id: str | None = Field(None, alias="documentId")
    chunks: list[DocumentChunk] = []

class Prompt(AprilFlowModel):
    tenant_id: str | None = Field(None, alias="tenantId")
    id: str | None = None
    session_id: str | None = Field(None, alias="sessionId")
    status: PromptStatus | None = None
    status_date: datetime | None = Field(None, alias="statusDate")
    handler_type: PromptHandlerType | None = Field(None, alias="handlerType")
    prompt_date: datetime | None = Field(None, alias="promptDate")
    input: str | None = None
    output: str | None = None
    duration_millis: int | None = Field(None, alias="durationMillis")
    collection_ids: list[str] = Field(default_factory=list, alias="collectionIds")
    upload_ids: list[PromptUploadId] = Field(default_factory=list, alias="uploadIds")
    documents: list[RetrievedDocument] = []

class Session(AprilFlowModel):
    tenant_id: str | None = Field(None, alias="tenantId")
    id: str | None = None
    user_id: str | None = Field(None, alias="userId")
    title: str | None = None
    modified_date: datetime | None = Field(None, alias="modifiedDate")

class CreateSessionResult(AprilFlowModel):
    session: Session
    prompt: Prompt

class PromptRequest(AprilFlowModel):
    text: str
    collection_ids: list[str] = Field(default_factory=list, alias="collectionIds")
    upload_ids: list[PromptUploadId] = Field(default_factory=list, alias="uploadIds")

class SessionListRequest(AprilFlowModel):
    first_result: int | None = Field(None, alias="firstResult")
    max_result: int | None = Field(None, alias="maxResult")

class UserCollection(AprilFlowModel):
    tenant_id: str | None = Field(None, alias="tenantId")
    user_id: str | None = Field(None, alias="userId")
    collection_id: str | None = Field(None, alias="collectionId")


class PromptWaitOptions(AprilFlowModel):
    timeout: float = 300.0
    terminal_statuses: list[PromptStatus] = Field(
        default_factory=lambda: [
            PromptStatus.COMPLETED,
            PromptStatus.INTERRUPTED,
            PromptStatus.ON_ERROR,
            PromptStatus.QUOTA_EXCEEDED,
        ],
        alias="terminalStatuses",
    )
