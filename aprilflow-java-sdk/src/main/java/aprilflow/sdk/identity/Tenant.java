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

public final class Tenant
{
    private final String id;
    private final Instant registrationDate;
    private final TenantStatus status;
    private final Instant statusDate;

    @JsonCreator
    public Tenant(
        @JsonProperty("id") String id,
        @JsonProperty("registrationDate") Instant registrationDate,
        @JsonProperty("status") TenantStatus status,
        @JsonProperty("statusDate") Instant statusDate
    )
    {
        this.id = id;

        this.registrationDate = registrationDate;

        this.status = status;

        this.statusDate = statusDate;
    }

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("registrationDate")
    public Instant getRegistrationDate()
    {
        return registrationDate;
    }

    @JsonProperty("status")
    public TenantStatus getStatus()
    {
        return status;
    }

    @JsonProperty("statusDate")
    public Instant getStatusDate()
    {
        return statusDate;
    }
}
