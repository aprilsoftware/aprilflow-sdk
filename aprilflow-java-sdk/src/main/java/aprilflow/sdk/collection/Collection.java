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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public final class Collection
{
    private final String id;
    private final String tenantId;
    private final Instant creationDate;
    private final CollectionStatus status;
    private final Instant statusDate;
    private final String name;
    private final String description;
    private final CollectionVisibility visibility;
    private final String code;
    private final String userId;

    @JsonCreator
    public Collection(
        @JsonProperty("id") String id,
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("creationDate") Instant creationDate,
        @JsonProperty("status") CollectionStatus status,
        @JsonProperty("statusDate") Instant statusDate,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("visibility") CollectionVisibility visibility,
        @JsonProperty("code") String code,
        @JsonProperty("userId") String userId
    )
    {
        this.id = id;
        this.tenantId = tenantId;
        this.creationDate = creationDate;
        this.status = status;
        this.statusDate = statusDate;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.code = code;
        this.userId = userId;
    }

    public String getId()
    {
        return id;
    }

    public String getTenantId()
    {
        return tenantId;
    }

    public Instant getCreationDate()
    {
        return creationDate;
    }

    public CollectionStatus getStatus()
    {
        return status;
    }

    public Instant getStatusDate()
    {
        return statusDate;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public CollectionVisibility getVisibility()
    {
        return visibility;
    }

    public String getCode()
    {
        return code;
    }

    public String getUserId()
    {
        return userId;
    }
}
