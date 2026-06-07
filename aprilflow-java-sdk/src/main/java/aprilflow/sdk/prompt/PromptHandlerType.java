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
import com.fasterxml.jackson.annotation.JsonValue;

public enum PromptHandlerType
{
    Direct,
    Reasoner;

    @JsonValue
    public String getName()
    {
        return name();
    }

    @JsonCreator
    public static PromptHandlerType fromName(String value)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }

        for (PromptHandlerType handlerType : values())
        {
            if (handlerType.name().equalsIgnoreCase(value))
            {
                return handlerType;
            }
        }

        throw new IllegalArgumentException("Unknown prompt handler type: " + value);
    }
}
