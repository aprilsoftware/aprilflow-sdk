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

import aprilflow.sdk.http.ApiPath;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.json.JsonSerializer;
import aprilflow.sdk.notification.NotificationApi;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class PromptApi
{
    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;
    private final SessionApi session;

    public PromptApi(AprilFlowHttpClient httpClient,
            JsonSerializer jsonSerializer,
            NotificationApi notificationApi)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;

        this.session = new SessionApi(httpClient, jsonSerializer, notificationApi);
    }

    public SessionApi session()
    {
        return session;
    }

    public String builtWith()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/prompt/v1/pub/system/built-with")
                    .build()
                    .toHttpRequest()
            )
        );

        return response.body();
    }

    public List<UserCollection> listUserCollections()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/prompt/v1/pub/users/collections")
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<UserCollection>>()
            {
            }
        );
    }

    public void setUserCollections(List<String> collectionIds)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/prompt/v1/pub/users/collections")
                    .json(jsonSerializer.serialize(collectionIds))
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public List<Prompt> listBySession(String sessionId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/prompt/v1/pub/prompts/sessions/" + ApiPath.segment(sessionId))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<Prompt>>()
            {
            }
        );
    }

    public Prompt restart(String promptId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/prompt/v1/pub/prompts/" + ApiPath.segment(promptId) + "/restart")
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Prompt.class);
    }

    public Prompt interrupt(String promptId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/prompt/v1/pub/prompts/" + ApiPath.segment(promptId) + "/interrupt")
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Prompt.class);
    }

    public void delete(String promptId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete("/prompt/v1/pub/prompts/" + ApiPath.segment(promptId))
                    .build()
                    .toHttpRequest()
            )
        );
    }
}
