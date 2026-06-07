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
package aprilflow.sdk.document;

import aprilflow.sdk.http.ApiPath;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.json.JsonSerializer;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class DocumentApi
{
    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;

    public DocumentApi(AprilFlowHttpClient httpClient, JsonSerializer jsonSerializer)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;
    }

    public List<DocumentItem> batchItems(List<DocumentId> documentIds)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post("/collection/v1/pub/documents/ids")
                    .json(jsonSerializer.serialize(documentIds))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<DocumentItem>>()
            {
            }
        );
    }

    public List<DocumentItem> search(DocumentSearchRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of(
                "/collection/v1/pub/"
                    + ApiPath.segment(request.collectionId())
                    + "/documents/search"
            )
            .query("maxResult", request.maxResult())
            .build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post(path)
                    .body(request.text())
                    .header("Content-Type", "application/json")
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<DocumentItem>>()
            {
            }
        );
    }

    public String getObjectAsString(String collectionId, String documentId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(documentObjectPath(collectionId, documentId))
                    .build()
                    .toHttpRequest()
            )
        );

        return response.body();
    }

    public byte[] getObjectAsBytes(String collectionId, String documentId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(documentObjectPath(collectionId, documentId))
                    .build()
                    .toHttpRequest()
            )
        );

        byte[] bodyBytes = response.bodyBytes();

        if (bodyBytes == null)
        {
            return new byte[0];
        }

        return bodyBytes;
    }

    public Document getOriginalDocument(String collectionId, String documentId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(
                    "/collection/v1/pub/"
                        + ApiPath.segment(collectionId)
                        + "/documents/"
                        + ApiPath.segment(documentId)
                        + "/original"
                )
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Document.class);
    }

    public void deleteOriginalDocument(String collectionId, String documentId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete(
                    "/collection/v1/pub/"
                        + ApiPath.segment(collectionId)
                        + "/documents/"
                        + ApiPath.segment(documentId)
                        + "/original"
                )
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public void delete(String collectionId, String documentId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete(
                    "/collection/v1/pub/"
                        + ApiPath.segment(collectionId)
                        + "/documents/"
                        + ApiPath.segment(documentId)
                )
                    .build()
                    .toHttpRequest()
            )
        );
    }

    private String documentObjectPath(String collectionId, String documentId)
    {
        return "/collection/v1/pub/"
            + ApiPath.segment(collectionId)
            + "/documents/"
            + ApiPath.segment(documentId)
            + "/object";
    }
}
