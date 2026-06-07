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
package aprilflow.sdk;

import aprilflow.sdk.auth.TokenProvider;
import aprilflow.sdk.auth.UserKeyTokenProvider;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.JerseyHttpClient;
import aprilflow.sdk.json.JacksonJsonSerializer;
import aprilflow.sdk.json.JsonSerializer;
import aprilflow.sdk.notification.AprilFlowNotificationClient;
import aprilflow.sdk.notification.JerseyNotificationClient;

public final class AprilFlowClientBuilder
{
    private String baseUrl;
    private String userKey;
    private AprilFlowHttpClient httpClient;
    private AprilFlowNotificationClient notificationClient;
    private JsonSerializer jsonSerializer;

    public AprilFlowClientBuilder()
    {
    }

    public AprilFlowClientBuilder baseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;

        return this;
    }

    public AprilFlowClientBuilder userKey(String userKey)
    {
        this.userKey = userKey;

        return this;
    }

    public AprilFlowClientBuilder httpClient(AprilFlowHttpClient httpClient)
    {
        this.httpClient = httpClient;

        return this;
    }

    public AprilFlowClientBuilder notificationClient(AprilFlowNotificationClient notificationClient)
    {
        this.notificationClient = notificationClient;

        return this;
    }

    public AprilFlowClientBuilder jsonSerializer(JsonSerializer jsonSerializer)
    {
        this.jsonSerializer = jsonSerializer;

        return this;
    }

    public AprilFlowClient build()
    {
        AprilFlowHttpClient resolvedHttpClient;
        AprilFlowNotificationClient resolvedNotificationClient;
        JsonSerializer resolvedJsonSerializer;
        TokenProvider tokenProvider;

        resolvedHttpClient = httpClient;

        if (resolvedHttpClient == null)
        {
            resolvedHttpClient = new JerseyHttpClient(baseUrl);
        }

        resolvedNotificationClient = notificationClient;

        if (resolvedNotificationClient == null)
        {
            resolvedNotificationClient = new JerseyNotificationClient(baseUrl);
        }

        if (userKey != null && !userKey.isBlank())
        {
            tokenProvider = new UserKeyTokenProvider(resolvedHttpClient, userKey);
        }
        else
        {
            throw new AprilFlowException("Unable to determine which token provider should be used.");
        }

        resolvedJsonSerializer = jsonSerializer;

        if (resolvedJsonSerializer == null)
        {
            resolvedJsonSerializer = new JacksonJsonSerializer();
        }

        return new AprilFlowClient(baseUrl,
                tokenProvider,
                resolvedHttpClient,
                resolvedNotificationClient,
                resolvedJsonSerializer);
    }
}
