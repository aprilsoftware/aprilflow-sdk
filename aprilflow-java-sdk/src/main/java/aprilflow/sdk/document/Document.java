/*
 * Copyright 2026 April Software
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package aprilflow.sdk.document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public final class Document
{
    private final String collectionId;
    private final String id;
    private final DocumentType documentType;
    private final Instant creationDate;
    private final DocumentStatus status;
    private final Instant statusDate;
    private final String fileName;
    private final int fileSize;

    @JsonCreator
    public Document(
        @JsonProperty("collectionId") String collectionId,
        @JsonProperty("id") String id,
        @JsonProperty("documentType") DocumentType documentType,
        @JsonProperty("creationDate") Instant creationDate,
        @JsonProperty("status") DocumentStatus status,
        @JsonProperty("statusDate") Instant statusDate,
        @JsonProperty("fileName") String fileName,
        @JsonProperty("fileSize") int fileSize
    )
    {
        this.collectionId = collectionId;

        this.id = id;

        this.documentType = documentType;

        this.creationDate = creationDate;

        this.status = status;

        this.statusDate = statusDate;

        this.fileName = fileName;

        this.fileSize = fileSize;
    }

    public String getCollectionId()
    {
        return collectionId;
    }

    public String getId()
    {
        return id;
    }

    public DocumentType getDocumentType()
    {
        return documentType;
    }

    public Instant getCreationDate()
    {
        return creationDate;
    }

    public DocumentStatus getStatus()
    {
        return status;
    }

    public Instant getStatusDate()
    {
        return statusDate;
    }

    public String getFileName()
    {
        return fileName;
    }

    public int getFileSize()
    {
        return fileSize;
    }
}
