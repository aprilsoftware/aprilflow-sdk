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

class UploadStatus(str, Enum):
    UPLOADED = "Uploaded"
    PROCESSING = "Processing"
    CANCELED = "Canceled"
    PROCESSED = "Processed"
    IGNORED = "Ignored"
    ON_ERROR = "OnError"
    DOCUMENT_DELETED = "DocumentDeleted"
    QUOTA_EXCEEDED = "QuotaExceeded"

class Upload(AprilFlowModel):
    id: str | None = None
    collection_id: str | None = Field(None, alias="collectionId")
    upload_date: datetime | None = Field(None, alias="uploadDate")
    status: UploadStatus | None = None
    status_date: datetime | None = Field(None, alias="statusDate")
    file_name: str | None = Field(None, alias="fileName")
    file_size: int | None = Field(None, alias="fileSize")
    document_id: str | None = Field(None, alias="documentId")
    processing_duration_millis: int | None = Field(None, alias="processingDurationMillis")

class UploadListRequest(AprilFlowModel):
    collection_id: str = Field(alias="collectionId")
    search: str | None = None
    first_result: int | None = Field(None, alias="firstResult")
    max_result: int | None = Field(None, alias="maxResult")
    statuses: list[UploadStatus] = []

class UploadBatchRequest(AprilFlowModel):
    collection_id: str = Field(alias="collectionId")
    upload_ids: list[str] = Field(alias="uploadIds")


class UploadWaitOptions(AprilFlowModel):
    timeout: float = 300.0
    terminal_statuses: list[UploadStatus] = Field(
        default_factory=lambda: [
            UploadStatus.PROCESSED,
            UploadStatus.IGNORED,
            UploadStatus.CANCELED,
            UploadStatus.ON_ERROR,
            UploadStatus.DOCUMENT_DELETED,
            UploadStatus.QUOTA_EXCEEDED,
        ],
        alias="terminalStatuses",
    )
