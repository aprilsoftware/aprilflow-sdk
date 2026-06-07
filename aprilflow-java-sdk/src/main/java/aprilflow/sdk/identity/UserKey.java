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
package aprilflow.sdk.identity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public final class UserKey
{
    private final String tenantId;
    private final String keyId;
    private final String userId;
    private final Instant creationDate;
    private final Instant lastUsedDate;
    private final Instant revokedDate;
    private final int usageCount;

    @JsonCreator
    public UserKey(
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("keyId") String keyId,
        @JsonProperty("userId") String userId,
        @JsonProperty("creationDate") Instant creationDate,
        @JsonProperty("lastUsedDate") Instant lastUsedDate,
        @JsonProperty("revokedDate") Instant revokedDate,
        @JsonProperty("usageCount") int usageCount
    )
    {
        this.tenantId = tenantId;
        this.keyId = keyId;
        this.userId = userId;
        this.creationDate = creationDate;
        this.lastUsedDate = lastUsedDate;
        this.revokedDate = revokedDate;
        this.usageCount = usageCount;
    }

    @JsonProperty("tenantId")
    public String getTenantId()
    {
        return tenantId;
    }

    @JsonProperty("keyId")
    public String getKeyId()
    {
        return keyId;
    }

    @JsonProperty("userId")
    public String getUserId()
    {
        return userId;
    }

    @JsonProperty("creationDate")
    public Instant getCreationDate()
    {
        return creationDate;
    }

    @JsonProperty("lastUsedDate")
    public Instant getLastUsedDate()
    {
        return lastUsedDate;
    }

    @JsonProperty("revokedDate")
    public Instant getRevokedDate()
    {
        return revokedDate;
    }

    @JsonProperty("usageCount")
    public int getUsageCount()
    {
        return usageCount;
    }
}
