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

public final class UserCollection
{
    private final String tenantId;
    private final String userId;
    private final String collectionId;

    @JsonCreator
    public UserCollection(
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("userId") String userId,
        @JsonProperty("collectionId") String collectionId
    )
    {
        this.tenantId = tenantId;

        this.userId = userId;

        this.collectionId = collectionId;
    }

    @JsonProperty("tenantId")
    public String getTenantId()
    {
        return tenantId;
    }

    @JsonProperty("userId")
    public String getUserId()
    {
        return userId;
    }

    @JsonProperty("collectionId")
    public String getCollectionId()
    {
        return collectionId;
    }
}
