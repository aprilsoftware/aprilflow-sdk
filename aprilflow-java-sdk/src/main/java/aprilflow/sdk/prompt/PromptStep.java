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

public final class PromptStep
{
    private final String tenantId;
    private final String id;
    private final String promptId;
    private final int index;
    private final String description;
    private final List<RetrievedDocument> documents;

    @JsonCreator
    public PromptStep(
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("id") String id,
        @JsonProperty("promptId") String promptId,
        @JsonProperty("index") int index,
        @JsonProperty("description") String description,
        @JsonProperty("documents") List<RetrievedDocument> documents
    )
    {
        this.tenantId = tenantId;

        this.id = id;

        this.promptId = promptId;

        this.index = index;

        this.description = description;

        this.documents = documents;
    }

    @JsonProperty("tenantId")
    public String getTenantId()
    {
        return tenantId;
    }

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("promptId")
    public String getPromptId()
    {
        return promptId;
    }

    @JsonProperty("index")
    public int getIndex()
    {
        return index;
    }

    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    @JsonProperty("documents")
    public List<RetrievedDocument> getDocuments()
    {
        return documents;
    }
}
