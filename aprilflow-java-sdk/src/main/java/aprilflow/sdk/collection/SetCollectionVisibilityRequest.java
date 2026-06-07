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
package aprilflow.sdk.collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class SetCollectionVisibilityRequest
{
    private String collectionId;
    private CollectionVisibility visibility;
    private List<String> policyIds;

    private SetCollectionVisibilityRequest()
    {
        policyIds = new ArrayList<>();
    }

    public static SetCollectionVisibilityRequest create()
    {
        return new SetCollectionVisibilityRequest();
    }

    public SetCollectionVisibilityRequest collectionId(String collectionId)
    {
        this.collectionId = collectionId;

        return this;
    }

    public SetCollectionVisibilityRequest visibility(CollectionVisibility visibility)
    {
        this.visibility = visibility;

        return this;
    }

    public SetCollectionVisibilityRequest policyIds(List<String> policyIds)
    {
        this.policyIds = policyIds;

        return this;
    }

    @JsonIgnore
    public String collectionId()
    {
        return collectionId;
    }

    @JsonProperty("visibility")
    public CollectionVisibility visibility()
    {
        return visibility;
    }

    @JsonProperty("policyIds")
    public List<String> policyIds()
    {
        return policyIds;
    }
}
