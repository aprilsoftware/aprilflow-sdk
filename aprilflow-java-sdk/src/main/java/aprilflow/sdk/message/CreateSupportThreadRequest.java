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
package aprilflow.sdk.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateSupportThreadRequest
{
    private String topic;
    private String email;
    private String message;

    private CreateSupportThreadRequest()
    {
    }

    public static CreateSupportThreadRequest create()
    {
        return new CreateSupportThreadRequest();
    }

    public CreateSupportThreadRequest topic(String topic)
    {
        this.topic = topic;

        return this;
    }

    public CreateSupportThreadRequest email(String email)
    {
        this.email = email;

        return this;
    }

    public CreateSupportThreadRequest message(String message)
    {
        this.message = message;

        return this;
    }

    @JsonProperty("topic")
    public String topic()
    {
        return topic;
    }

    @JsonProperty("email")
    public String email()
    {
        return email;
    }

    @JsonProperty("message")
    public String message()
    {
        return message;
    }
}
