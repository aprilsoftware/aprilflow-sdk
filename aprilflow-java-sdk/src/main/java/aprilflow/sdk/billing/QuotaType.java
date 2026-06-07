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
import com.fasterxml.jackson.annotation.JsonProperty;

public final class QuotaType
{
    private final String id;
    private final String code;
    private final String name;

    @JsonCreator
    public QuotaType(
        @JsonProperty("id") String id,
        @JsonProperty("code") String code,
        @JsonProperty("name") String name
    )
    {
        this.id = id;

        this.code = code;

        this.name = name;
    }

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("code")
    public String getCode()
    {
        return code;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }
}
