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
package aprilflow.sdk.billing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class Quota
{
    private final String id;
    private final String tenantId;
    private final QuotaTarget quotaTarget;
    private final QuotaScope quotaScope;
    private final String usageId;
    private final String quotaTypeId;
    private final int quotaWindow;
    private final String description;
    private final int value;

    @JsonCreator
    public Quota(
        @JsonProperty("id") String id,
        @JsonProperty("tenantId") String tenantId,
        @JsonProperty("quotaTarget") QuotaTarget quotaTarget,
        @JsonProperty("quotaScope") QuotaScope quotaScope,
        @JsonProperty("usageId") String usageId,
        @JsonProperty("quotaTypeId") String quotaTypeId,
        @JsonProperty("quotaWindow") int quotaWindow,
        @JsonProperty("description") String description,
        @JsonProperty("value") int value
    )
    {
        this.id = id;

        this.tenantId = tenantId;

        this.quotaTarget = quotaTarget;

        this.quotaScope = quotaScope;

        this.usageId = usageId;

        this.quotaTypeId = quotaTypeId;

        this.quotaWindow = quotaWindow;

        this.description = description;

        this.value = value;
    }

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("tenantId")
    public String getTenantId()
    {
        return tenantId;
    }

    @JsonProperty("quotaTarget")
    public QuotaTarget getQuotaTarget()
    {
        return quotaTarget;
    }

    @JsonProperty("quotaScope")
    public QuotaScope getQuotaScope()
    {
        return quotaScope;
    }

    @JsonProperty("usageId")
    public String getUsageId()
    {
        return usageId;
    }

    @JsonProperty("quotaTypeId")
    public String getQuotaTypeId()
    {
        return quotaTypeId;
    }

    @JsonProperty("quotaWindow")
    public int getQuotaWindow()
    {
        return quotaWindow;
    }

    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    @JsonProperty("value")
    public int getValue()
    {
        return value;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private String id;
        private String tenantId;
        private QuotaTarget quotaTarget;
        private QuotaScope quotaScope;
        private String usageId;
        private String quotaTypeId;
        private int quotaWindow;
        private String description;
        private int value;

        private Builder()
        {
        }

        public Builder id(String id)
        {
            this.id = id;

            return this;
        }

        public Builder tenantId(String tenantId)
        {
            this.tenantId = tenantId;

            return this;
        }

        public Builder quotaTarget(QuotaTarget quotaTarget)
        {
            this.quotaTarget = quotaTarget;

            return this;
        }

        public Builder quotaScope(QuotaScope quotaScope)
        {
            this.quotaScope = quotaScope;

            return this;
        }

        public Builder usageId(String usageId)
        {
            this.usageId = usageId;

            return this;
        }

        public Builder quotaTypeId(String quotaTypeId)
        {
            this.quotaTypeId = quotaTypeId;

            return this;
        }

        public Builder quotaWindow(int quotaWindow)
        {
            this.quotaWindow = quotaWindow;

            return this;
        }

        public Builder description(String description)
        {
            this.description = description;

            return this;
        }

        public Builder value(int value)
        {
            this.value = value;

            return this;
        }

        public Quota build()
        {
            return new Quota(
                id,
                tenantId,
                quotaTarget,
                quotaScope,
                usageId,
                quotaTypeId,
                quotaWindow,
                description,
                value
            );
        }
    }
}
