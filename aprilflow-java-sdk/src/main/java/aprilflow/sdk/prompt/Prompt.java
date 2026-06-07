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

import java.time.Instant;
import java.util.List;

public final class Prompt
{
    private final String tenantId;
    private final String id;
    private final String sessionId;
    private final PromptStatus status;
    private final Instant statusDate;
    private final PromptHandlerType handlerType;
    private final Instant promptDate;
    private final String input;
    private final String output;
    private final Integer durationMillis;
    private final List<String> collectionIds;
    private final List<PromptUploadId> uploadIds;
    private final List<RetrievedDocument> documents;

    @JsonCreator
    public Prompt(
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("id") String id,
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("status") PromptStatus status,
        @JsonProperty("statusDate") Instant statusDate,
        @JsonProperty("handlerType") PromptHandlerType handlerType,
        @JsonProperty("promptDate") Instant promptDate,
        @JsonProperty("input") String input,
        @JsonProperty("output") String output,
        @JsonProperty("durationMillis") Integer durationMillis,
        @JsonProperty("collectionIds") List<String> collectionIds,
        @JsonProperty("uploadIds") List<PromptUploadId> uploadIds,
        @JsonProperty("documents") List<RetrievedDocument> documents
    )
    {
        this.tenantId = tenantId;

        this.id = id;

        this.sessionId = sessionId;

        this.status = status;

        this.statusDate = statusDate;

        this.handlerType = handlerType;

        this.promptDate = promptDate;

        this.input = input;

        this.output = output;

        this.durationMillis = durationMillis;

        this.collectionIds = collectionIds;

        this.uploadIds = uploadIds;

        this.documents = documents;
    }

    public String getTenantId()
    {
        return tenantId;
    }

    public String getId()
    {
        return id;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public PromptStatus getStatus()
    {
        return status;
    }

    public Instant getStatusDate()
    {
        return statusDate;
    }

    public PromptHandlerType getHandlerType()
    {
        return handlerType;
    }

    public Instant getPromptDate()
    {
        return promptDate;
    }

    public String getInput()
    {
        return input;
    }

    public String getOutput()
    {
        return output;
    }

    public Integer getDurationMillis()
    {
        return durationMillis;
    }

    public List<String> getCollectionIds()
    {
        return collectionIds;
    }

    public List<PromptUploadId> getUploadIds()
    {
        return uploadIds;
    }

    public List<RetrievedDocument> getDocuments()
    {
        return documents;
    }
}
