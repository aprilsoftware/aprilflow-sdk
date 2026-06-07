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

import aprilflow.sdk.http.ApiPath;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.json.JsonSerializer;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class UserApi
{
    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;

    public UserApi(AprilFlowHttpClient httpClient, JsonSerializer jsonSerializer)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;
    }

    public User get(String userId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/identity/v1/pub/tenants/users/" + ApiPath.segment(userId))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), User.class);
    }

    public int count(UserListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/identity/v1/pub/tenants/users/count")
                .query("search", request.search())
                .build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(path)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Integer.class);
    }

    public List<User> list(UserListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/identity/v1/pub/tenants/users")
                .query("search", request.search())
                .query("firstResult", request.firstResult())
                .query("maxResult", request.maxResult())
                .build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(path)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<User>>()
            {
            }
        );
    }

    public User invite(String email, List<String> policyIds)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/identity/v1/pub/tenants/users/invite")
            .query("email", email)
            .build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post(path)
                    .json(jsonSerializer.serialize(policyIds))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), User.class);
    }

    public void updateInfo(User user)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/identity/v1/pub/tenants/users/" + ApiPath.segment(user.getId()))
                    .json(jsonSerializer.serialize(user))
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public void delete(String userId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete("/identity/v1/pub/tenants/users/" + ApiPath.segment(userId))
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public User updatePolicies(String userId, List<String> policyIds)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/identity/v1/pub/tenants/users/" + ApiPath.segment(userId) + "/policies")
                    .json(jsonSerializer.serialize(policyIds))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), User.class);
    }

    public User setEnabled(String userId, boolean enabled)
    {
        HttpResponse response;
        String action;

        action = enabled ? "enable" : "disable";

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/identity/v1/pub/tenants/users/" + ApiPath.segment(userId) + "/" + action)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), User.class);
    }

    public User enable(String userId)
    {
        return setEnabled(userId, true);
    }

    public User disable(String userId)
    {
        return setEnabled(userId, false);
    }

    public User setTotpEnabled(String userId, boolean enabled)
    {
        HttpResponse response;
        String action;

        action = enabled ? "enable" : "disable";

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/identity/v1/pub/tenants/users/" + ApiPath.segment(userId) + "/totp/" + action)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), User.class);
    }

    public User enableTotp(String userId)
    {
        return setTotpEnabled(userId, true);
    }

    public User disableTotp(String userId)
    {
        return setTotpEnabled(userId, false);
    }

    public void changeEmail(String userId, String email)
    {
        String path = ApiPath.of("/identity/v1/pub/tenants/users/" + ApiPath.segment(userId) + "/email")
            .query("id", userId)
            .query("email", email)
            .build();

        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put(path)
                    .build()
                    .toHttpRequest()
            )
        );
    }
}
