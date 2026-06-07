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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Policy
{
    private final String id;
    private final String name;
    private final String description;
    private final String tenantId;
    private final List<TenantRole> roles;

    @JsonCreator
    public Policy(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("roles") List<TenantRole> roles
    )
    {
        this.id = id;

        this.name = name;

        this.description = description;

        this.tenantId = tenantId;

        this.roles = roles;
    }

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    @JsonProperty("tenantId")
    public String getTenantId()
    {
        return tenantId;
    }

    @JsonProperty("roles")
    public List<TenantRole> getRoles()
    {
        return roles;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String id;
        private String name;
        private String description;
        private String tenantId;
        private List<TenantRole> roles;

        private Builder()
        {
            roles = new ArrayList<>();
        }

        public Builder id(String id)
        {
            this.id = id;

            return this;
        }

        public Builder name(String name)
        {
            this.name = name;

            return this;
        }

        public Builder description(String description)
        {
            this.description = description;

            return this;
        }

        public Builder tenantId(String tenantId)
        {
            this.tenantId = tenantId;

            return this;
        }

        public Builder roles(List<TenantRole> roles)
        {
            this.roles = roles;

            return this;
        }

        public Policy build()
        {
            return new Policy(
                id,
                name,
                description,
                tenantId,
                roles
            );
        }
    }
}
