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

public final class PolicyApi
{
    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;

    public PolicyApi(AprilFlowHttpClient httpClient, JsonSerializer jsonSerializer)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;
    }

    public int count(PolicyListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/identity/v1/pub/tenants/policies/count")
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

    public List<Policy> list(PolicyListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/identity/v1/pub/tenants/policies")
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
            new TypeReference<List<Policy>>()
            {
            }
        );
    }

    public Policy insert(Policy policy)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post("/identity/v1/pub/tenants/policies")
                    .json(jsonSerializer.serialize(policy))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Policy.class);
    }

    public Policy update(Policy policy)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/identity/v1/pub/tenants/policies")
                    .json(jsonSerializer.serialize(policy))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Policy.class);
    }

    public void delete(String policyId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete("/identity/v1/pub/tenants/policies/" + ApiPath.segment(policyId))
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public List<User> listUsers(String policyId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/identity/v1/pub/tenants/policies/" + ApiPath.segment(policyId) + "/users")
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

    public void removeUsers(String policyId, List<String> userIds)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete("/identity/v1/pub/tenants/policies/" + ApiPath.segment(policyId) + "/users")
                    .json(jsonSerializer.serialize(userIds))
                    .build()
                    .toHttpRequest()
            )
        );
    }
}
