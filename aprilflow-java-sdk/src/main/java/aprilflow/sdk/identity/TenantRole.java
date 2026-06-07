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
import com.fasterxml.jackson.annotation.JsonValue;

public enum TenantRole
{
    TenantRead("TenantRead"),
    TenantUpdate("TenantUpdate"),
    TenantDelete("TenantDelete"),

    UserCreate("UserCreate"),
    UserRead("UserRead"),
    UserUpdate("UserUpdate"),
    UserDelete("UserDelete"),

    PolicyCreate("PolicyCreate"),
    PolicyRead("PolicyRead"),
    PolicyUpdate("PolicyUpdate"),
    PolicyDelete("PolicyDelete"),

    CollectionCreate("CollectionCreate"),
    CollectionRead("CollectionRead"),
    CollectionUpdate("CollectionUpdate"),
    CollectionDelete("CollectionDelete"),

    PromptCreate("PromptCreate"),
    PromptRead("PromptRead"),
    PromptUpdate("PromptUpdate"),
    PromptDelete("PromptDelete"),

    UserKeyCreate("UserKeyCreate"),
    UserKeyRead("UserKeyRead"),
    UserKeyUpdate("UserKeyUpdate"),
    UserKeyDelete("UserKeyDelete"),

    QuotaCreate("QuotaCreate"),
    QuotaRead("QuotaRead"),
    QuotaUpdate("QuotaUpdate"),
    QuotaDelete("QuotaDelete");

    private final String name;

    private TenantRole(String name)
    {
        this.name = name;
    }

    @JsonValue
    public String getName()
    {
        return name;
    }

    @JsonCreator
    public static TenantRole fromName(String name)
    {
        if (name == null || name.isBlank())
        {
            return null;
        }

        for (TenantRole role : values())
        {
            if (role.name.equalsIgnoreCase(name))
            {
                return role;
            }
        }

        throw new IllegalArgumentException("Unknown tenant role: " + name);
    }
}
