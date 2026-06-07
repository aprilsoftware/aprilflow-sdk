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
package aprilflow.sdk.auth;

import java.time.Duration;
import java.time.Instant;

import aprilflow.sdk.AprilFlowException;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.identity.IdentityToken;

public final class UserKeyTokenProvider implements TokenProvider
{
    private final String userKey;
    private final AprilFlowHttpClient httpClient;
    private IdentityToken accessToken;
    private IdentityToken refreshToken;

    public UserKeyTokenProvider(AprilFlowHttpClient httpClient, String userKey)
    {
        this.httpClient = httpClient;

        this.userKey = userKey;
    }

    public synchronized String accessToken()
    {
        if (accessToken == null)
        {
            accessToken = getAccessToken();

            refreshToken = getRefreshToken();
        }

        if (accessToken.isExpired())
        {
            if (refreshToken.isExpired())
            {
                throw new AprilFlowException("Session expired.");
            }
            else
            {
                accessToken = refreshAccessToken();

                if (Instant.now()
                        .minus(Duration.ofDays(1))
                        .isBefore(refreshToken.getExpirationTime()))
                {
                    refreshToken = getRefreshToken();
                }
            }
        }

        return accessToken.getEncodedToken();
    }

    public synchronized void invalidate()
    {
        accessToken = null;

        refreshToken = null;
    }

    private IdentityToken getAccessToken()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/identity/v1/pub/tenants/auth/token/key")
                    .header("Authorization", "Bearer " + userKey)
                    .build()
                    .toHttpRequest()
            )
        );

        return IdentityToken.createToken(response.body());
    }

    private IdentityToken getRefreshToken()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/identity/v1/pub/tenants/auth/token/refresh")
                    .header("Authorization", "Bearer " + accessToken.getEncodedToken())
                    .build()
                    .toHttpRequest()
            )
        );

        return IdentityToken.createToken(response.body());
    }

    private IdentityToken refreshAccessToken()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/identity/v1/pub/tenants/auth/refresh")
                    .header("Authorization", "Bearer " + refreshToken.getEncodedToken())
                    .build()
                    .toHttpRequest()
            )
        );

        return IdentityToken.createToken(response.body());
    }
}
