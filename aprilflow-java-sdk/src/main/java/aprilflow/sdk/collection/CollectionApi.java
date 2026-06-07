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
package aprilflow.sdk.collection;

import aprilflow.sdk.http.ApiPath;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.json.JsonSerializer;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class CollectionApi
{
    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;

    public CollectionApi(AprilFlowHttpClient httpClient, JsonSerializer jsonSerializer)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;
    }

    public List<Collection> listByPermissions()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/collection/v1/pub/collections/by-permissions")
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<Collection>>()
            {
            }
        );
    }

    public int count(CollectionListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/collection/v1/pub/collections/count")
            .query("search", request.search())
            .build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(path)
                    .build()
                    .toHttpRequest()
            )
        );

        return Integer.parseInt(response.body());
    }

    public List<Collection> list(CollectionListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/collection/v1/pub/collections")
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
            new TypeReference<List<Collection>>()
            {
            }
        );
    }

    public int countDeleted()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/collection/v1/pub/collections/deleted/count")
                    .build()
                    .toHttpRequest()
            )
        );

        return Integer.parseInt(response.body());
    }

    public List<Collection> listDeleted(Integer firstResult, Integer maxResult)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/collection/v1/pub/collections/deleted")
            .query("firstResult", firstResult)
            .query("maxResult", maxResult)
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
            new TypeReference<List<Collection>>()
            {
            }
        );
    }

    public List<Collection> restore(List<String> collectionIds)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/collection/v1/pub/collections/deleted/restore")
                    .json(jsonSerializer.serialize(collectionIds))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<Collection>>()
            {
            }
        );
    }

    public Collection create(CreateCollectionRequest request)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post("/collection/v1/pub/collections")
                    .json(jsonSerializer.serialize(request))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Collection.class);
    }

    public Collection update(UpdateCollectionRequest request)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/collection/v1/pub/collections/" + ApiPath.segment(request.collectionId()))
                    .json(jsonSerializer.serialize(request))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Collection.class);
    }

    public List<String> listPolicyIds(String collectionId)
    {
        HttpResponse response;
        List<String> policyIds;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/collection/v1/pub/collections/" + ApiPath.segment(collectionId) + "/visibility")
                    .build()
                    .toHttpRequest()
            )
        );

        policyIds = jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<String>>()
            {
            }
        );

        return policyIds;
    }

    public Collection setVisibility(SetCollectionVisibilityRequest request)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/collection/v1/pub/collections/" + ApiPath.segment(request.collectionId()) + "/visibility")
                    .json(jsonSerializer.serialize(request))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Collection.class);
    }

    public void delete(String collectionId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete("/collection/v1/pub/collections/" + ApiPath.segment(collectionId))
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public Collection getUserPromptCollection()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/collection/v1/pub/collections/user/prompt")
                    .build()
                    .toHttpRequest()
            )
        );

        if (response.statusCode() == 204 || response.body() == null || response.body().isBlank())
        {
            return null;
        }

        return jsonSerializer.deserialize(response.body(), Collection.class);
    }

    public Collection createUserPromptCollection()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post("/collection/v1/pub/collections/user/prompt")
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Collection.class);
    }
}
