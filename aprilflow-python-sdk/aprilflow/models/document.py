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

class DocumentStatus(str, Enum):
    CREATED = "Created"
    PROCESSED = "Processed"
    DELETED = "Deleted"

class DocumentType(str, Enum):
    ORIGINAL = "Original"
    MARKDOWN = "Markdown"

class Chunk(AprilFlowModel):
    collection_id: str | None = Field(None, alias="collectionId")
    chunk_id: str | None = Field(None, alias="chunkId")
    text: str | None = None
    start_line: int | None = Field(None, alias="startLine")
    end_line: int | None = Field(None, alias="endLine")
    score: float | None = None

class Document(AprilFlowModel):
    collection_id: str | None = Field(None, alias="collectionId")
    id: str | None = None
    document_type: DocumentType | None = Field(None, alias="documentType")
    creation_date: datetime | None = Field(None, alias="creationDate")
    status: DocumentStatus | None = None
    status_date: datetime | None = Field(None, alias="statusDate")
    file_name: str | None = Field(None, alias="fileName")
    file_size: int | None = Field(None, alias="fileSize")

class DocumentId(AprilFlowModel):
    collection_id: str = Field(alias="collectionId")
    document_id: str = Field(alias="documentId")

class DocumentItem(AprilFlowModel):
    document: Document | None = None
    overview: str | None = None
    score: float | None = None
    chunks: list[Chunk] = []

class DocumentSearchRequest(AprilFlowModel):
    collection_id: str = Field(alias="collectionId")
    text: str
    max_result: int | None = Field(None, alias="maxResult")
