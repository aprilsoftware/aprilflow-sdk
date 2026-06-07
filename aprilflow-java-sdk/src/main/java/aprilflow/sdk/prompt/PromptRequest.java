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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PromptRequest
{
    private String text;
    private final List<String> collectionIds;
    private final List<PromptUploadId> uploadIds;

    private PromptRequest()
    {
        this.collectionIds = new ArrayList<>();

        this.uploadIds = new ArrayList<>();
    }

    public static PromptRequest create()
    {
        return new PromptRequest();
    }

    public PromptRequest text(String text)
    {
        this.text = text;

        return this;
    }

    public PromptRequest collectionId(String collectionId)
    {
        this.collectionIds.add(collectionId);

        return this;
    }

    public PromptRequest collectionIds(List<String> collectionIds)
    {
        for (String collectionId : collectionIds)
        {
            collectionId(collectionId);
        }

        return this;
    }

    public PromptRequest uploadId(String collectionId, String uploadId)
    {
        this.uploadIds.add(PromptUploadId.of(collectionId, uploadId));

        return this;
    }

    public PromptRequest uploadId(PromptUploadId uploadId)
    {
        this.uploadIds.add(uploadId);

        return this;
    }

    public PromptRequest uploadIds(List<PromptUploadId> uploadIds)
    {
        for (PromptUploadId uploadId : uploadIds)
        {
            uploadId(uploadId);
        }

        return this;
    }

    @JsonProperty("text")
    public String text()
    {
        return text;
    }

    @JsonProperty("collectionIds")
    public List<String> collectionIds()
    {
        return collectionIds;
    }

    @JsonProperty("uploadIds")
    public List<PromptUploadId> uploadIds()
    {
        return uploadIds;
    }
}
