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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateCollectionRequest
{
    private String name;
    private String description;
    private CollectionVisibility visibility;

    private CreateCollectionRequest()
    {
    }

    public static CreateCollectionRequest create()
    {
        return new CreateCollectionRequest();
    }

    public CreateCollectionRequest name(String name)
    {
        this.name = name;

        return this;
    }

    public CreateCollectionRequest description(String description)
    {
        this.description = description;

        return this;
    }

    public CreateCollectionRequest visibility(CollectionVisibility visibility)
    {
        this.visibility = visibility;

        return this;
    }

    @JsonProperty("name")
    public String name()
    {
        return name;
    }

    @JsonProperty("description")
    public String description()
    {
        return description;
    }

    @JsonProperty("visibility")
    public CollectionVisibility visibility()
    {
        return visibility;
    }
}
