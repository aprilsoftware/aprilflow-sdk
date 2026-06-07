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
package aprilflow.sdk.json;

import aprilflow.sdk.AprilFlowException;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class JacksonJsonSerializer implements JsonSerializer
{
    private final ObjectMapper objectMapper;

    public JacksonJsonSerializer()
    {
        this(defaultObjectMapper());
    }

    public JacksonJsonSerializer(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object value)
    {
        if (value == null)
        {
            return null;
        }

        try
        {
            return objectMapper.writeValueAsString(value);
        }
        catch (Exception exception)
        {
            throw new AprilFlowException("Failed to serialize JSON body", exception);
        }
    }

    @Override
    public <T> T deserialize(String body, Class<T> type)
    {
        if (body == null || body.isBlank())
        {
            return null;
        }

        if (type == null)
        {
            throw new IllegalArgumentException("type must not be null");
        }

        try
        {
            return objectMapper.readValue(body, type);
        }
        catch (Exception exception)
        {
            throw new AprilFlowException("Failed to deserialize JSON body", exception);
        }
    }

    @Override
    public <T> T deserialize(String body, TypeReference<T> typeReference)
    {
        if (body == null || body.isBlank())
        {
            return null;
        }

        if (typeReference == null)
        {
            throw new IllegalArgumentException("typeReference must not be null");
        }

        try
        {
            return objectMapper.readValue(body, typeReference);
        }
        catch (Exception exception)
        {
            throw new AprilFlowException("Failed to deserialize JSON body", exception);
        }
    }

    public ObjectMapper objectMapper()
    {
        return objectMapper;
    }

    private static ObjectMapper defaultObjectMapper()
    {
        JavaTimeModule javaTimeModule;
        DateTimeFormatter formatter;
        ObjectMapper objectMapper;

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        javaTimeModule = new JavaTimeModule();

        javaTimeModule.addDeserializer(Instant.class, new JsonDeserializer<Instant>()
        {
            @Override
            public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException
            {
                return OffsetDateTime.parse(parser.getText(), formatter).toInstant();
            }
        });

        objectMapper = new ObjectMapper();

        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
}
