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
package aprilflow.sdk.http;

import aprilflow.sdk.auth.TokenProvider;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuthenticatedHttpClient implements AprilFlowHttpClient
{
    private final AprilFlowHttpClient httpClient;
    private final TokenProvider tokenProvider;

    public AuthenticatedHttpClient(AprilFlowHttpClient httpClient, TokenProvider tokenProvider)
    {
        this.httpClient = httpClient;

        this.tokenProvider = tokenProvider;
    }

    @Override
    public HttpResponse execute(HttpRequest request)
    {
        HttpResponse response;

        response = executeWithToken(request);

        if (response.statusCode() == 401)
        {
            tokenProvider.invalidate();

            response = executeWithToken(request);
        }

        return response;
    }

    private HttpResponse executeWithToken(HttpRequest request)
    {
        Map<String, String> headers;
        String accessToken;

        accessToken = tokenProvider.accessToken();

        headers = new LinkedHashMap<>(request.headers());

        headers.put("Authorization", "Bearer " + accessToken);

        return httpClient.execute(new HttpRequest(
            request.method(),
            request.path(),
            headers,
            request.body()
        ));
    }
}
