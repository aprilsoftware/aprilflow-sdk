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
import aprilflow.sdk.billing.BillingApi;
import aprilflow.sdk.collection.CollectionApi;
import aprilflow.sdk.document.DocumentApi;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.AuthenticatedHttpClient;
import aprilflow.sdk.identity.IdentityApi;
import aprilflow.sdk.json.JsonSerializer;
import aprilflow.sdk.message.MessageApi;
import aprilflow.sdk.notification.AprilFlowNotificationClient;
import aprilflow.sdk.notification.AuthenticatedNotificationClient;
import aprilflow.sdk.notification.JerseyNotificationClient;
import aprilflow.sdk.notification.NotificationApi;
import aprilflow.sdk.prompt.PromptApi;
import aprilflow.sdk.upload.UploadApi;

public final class AprilFlowClient
{
    private final String baseUrl;
    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;

    private final CollectionApi collection;
    private final UploadApi upload;
    private final DocumentApi document;
    private final PromptApi prompt;
    private final IdentityApi identity;
    private final BillingApi billing;
    private final MessageApi message;
    private final NotificationApi notification;

    public AprilFlowClient(String baseUrl,
            TokenProvider tokenProvider,
            AprilFlowHttpClient httpClient,
            JsonSerializer jsonSerializer)
    {
        this(baseUrl,
                tokenProvider,
                httpClient,
                new JerseyNotificationClient(baseUrl),
                jsonSerializer);
    }

    public AprilFlowClient(String baseUrl,
            TokenProvider tokenProvider,
            AprilFlowHttpClient httpClient,
            AprilFlowNotificationClient notificationClient,
            JsonSerializer jsonSerializer)
    {
        AuthenticatedNotificationClient authNotificationClient;
        AuthenticatedHttpClient authHttpClient;

        this.baseUrl = baseUrl;

        this.jsonSerializer = jsonSerializer;

        authHttpClient = new AuthenticatedHttpClient(httpClient, tokenProvider);

        authNotificationClient = new AuthenticatedNotificationClient(notificationClient, tokenProvider);

        this.httpClient = authHttpClient;

        this.notification = new NotificationApi(authNotificationClient);

        this.collection = new CollectionApi(authHttpClient, jsonSerializer);
        this.upload = new UploadApi(authHttpClient, jsonSerializer, notification);
        this.document = new DocumentApi(authHttpClient, jsonSerializer);
        this.prompt = new PromptApi(authHttpClient, jsonSerializer, notification);
        this.identity = new IdentityApi(authHttpClient, jsonSerializer);
        this.billing = new BillingApi(authHttpClient, jsonSerializer);
        this.message = new MessageApi(authHttpClient, jsonSerializer);
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public AprilFlowHttpClient httpClient()
    {
        return httpClient;
    }

    public JsonSerializer jsonSerializer()
    {
        return jsonSerializer;
    }

    public CollectionApi collection()
    {
        return collection;
    }

    public UploadApi upload()
    {
        return upload;
    }

    public DocumentApi document()
    {
        return document;
    }

    public PromptApi prompt()
    {
        return prompt;
    }

    public IdentityApi identity()
    {
        return identity;
    }

    public BillingApi billing()
    {
        return billing;
    }

    public MessageApi message()
    {
        return message;
    }

    public NotificationApi notification()
    {
        return notification;
    }

    public static AprilFlowClientBuilder builder()
    {
        return new AprilFlowClientBuilder();
    }

    public static AprilFlowClient create(String baseUrl, String userKey)
    {
        return builder()
            .baseUrl(baseUrl)
            .userKey(userKey)
            .build();
    }
}
