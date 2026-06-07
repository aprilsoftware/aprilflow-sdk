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

import aprilflow.sdk.AprilFlowException;

public final class ApiResponseHandler
{
    private ApiResponseHandler()
    {
    }

    public static HttpResponse requireSuccess(HttpResponse response)
    {
        if (response == null)
        {
            throw new AprilFlowException("HTTP response must not be null");
        }

        if (!response.isSuccessful())
        {
            throw new AprilFlowException(
                "April Flow API request failed with status " + response.statusCode(),
                response.statusCode(),
                response.body()
            );
        }

        return response;
    }
}
