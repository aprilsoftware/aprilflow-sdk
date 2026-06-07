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
import aprilflow.sdk.prompt.CreateSessionResult;
import aprilflow.sdk.prompt.PromptRequest;
import aprilflow.sdk.prompt.PromptStatus;
import aprilflow.sdk.prompt.PromptWaitOptions;
import aprilflow.sdk.prompt.Session;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class PromptTest extends BaseTest
{
    @Test
    void testPrompt()
    {
        AprilFlowClient client;
        CreateSessionResult result;
        Session session;

        client = initTest();

        session = null;

        try
        {
            result = client.prompt().session().createAndWait(
                PromptRequest.create()
                    .text("Please tell me a short story."),
                new PromptWaitOptions(
                    Duration.ofMinutes(3),
                    List.of(
                        PromptStatus.Completed,
                        PromptStatus.Interrupted,
                        PromptStatus.OnError,
                        PromptStatus.QuotaExceeded
                    )
                )
            );

            assertThat(result).isNotNull();
            assertThat(result.getSession()).isNotNull();
            assertThat(result.getPrompt()).isNotNull();
            assertThat(result.getSession().getId()).isNotBlank();
            assertThat(result.getPrompt().getId()).isNotBlank();

            session = result.getSession();

            assertThat(result.getPrompt().getSessionId()).isEqualTo(result.getSession().getId());
            assertThat(result.getPrompt().getStatus()).isEqualTo(PromptStatus.Completed);
            assertThat(result.getPrompt().getOutput()).isNotBlank();
        }
        finally
        {
            if (client != null)
            {
                if (session != null && session.getId() != null)
                {
                    try
                    {
                        client.prompt().session().delete(session.getId());
                    }
                    catch (RuntimeException ignored)
                    {
                    }
                }
            }
        }
    }
}
