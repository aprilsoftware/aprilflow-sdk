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
package aprilflow.sdk.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class RetrievedDocument
{
    private final String collectionId;
    private final String documentId;
    private final List<DocumentChunk> chunks;

    @JsonCreator
    public RetrievedDocument(
        @JsonProperty("collectionId") String collectionId,
        @JsonProperty("documentId") String documentId,
        @JsonProperty("chunks") List<DocumentChunk> chunks
    )
    {
        this.collectionId = collectionId;

        this.documentId = documentId;

        this.chunks = chunks;
    }

    public String getCollectionId()
    {
        return collectionId;
    }

    public String getDocumentId()
    {
        return documentId;
    }

    public List<DocumentChunk> getChunks()
    {
        return chunks;
    }
}
