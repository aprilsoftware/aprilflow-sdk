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

import aprilflow.sdk.http.ApiPath;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.json.JsonSerializer;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class QuotaApi
{
    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;

    public QuotaApi(AprilFlowHttpClient httpClient, JsonSerializer jsonSerializer)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;
    }

    public int count()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/billing/v1/pub/quotas/count")
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Integer.class);
    }

    public List<Quota> list(QuotaListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/billing/v1/pub/quotas")
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
            new TypeReference<List<Quota>>()
            {
            }
        );
    }

    public Quota insert(Quota quota)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post("/billing/v1/pub/quotas")
                    .json(jsonSerializer.serialize(quota))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Quota.class);
    }

    public Quota update(Quota quota)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/billing/v1/pub/quotas/" + ApiPath.segment(quota.getId()))
                    .json(jsonSerializer.serialize(quota))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Quota.class);
    }

    public void delete(String quotaId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete("/billing/v1/pub/quotas/" + ApiPath.segment(quotaId))
                    .build()
                    .toHttpRequest()
            )
        );
    }
}
