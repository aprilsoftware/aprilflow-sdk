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
package aprilflow.sdk.tests;

import aprilflow.sdk.AprilFlowClient;

import org.junit.jupiter.api.Assumptions;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseTest
{
    private static final String BASE_URL_ENV = "APRILFLOW_BASE_URL";
    private static final String USER_KEY_ENV = "APRILFLOW_USER_KEY";

    public BaseTest()
    {
    }

    protected AprilFlowClient initTest()
    {
        String baseUrl = System.getenv(BASE_URL_ENV);
        String userKey = System.getenv(USER_KEY_ENV);

        Assumptions.assumeTrue(
            baseUrl != null && !baseUrl.isBlank(),
            BASE_URL_ENV + " must be set to run tests"
        );

        Assumptions.assumeTrue(
            userKey != null && !userKey.isBlank(),
            USER_KEY_ENV + " must be set to run tests"
        );

        return AprilFlowClient.builder()
            .baseUrl(baseUrl)
            .userKey(userKey)
            .build();
    }

    protected String getTestName(String prefix)
    {
        String timestamp = Instant.now()
            .toString()
            .replace(":", "-")
            .replace(".", "-");

        return prefix + "-" + timestamp + "-" + UUID.randomUUID();
    }
}
