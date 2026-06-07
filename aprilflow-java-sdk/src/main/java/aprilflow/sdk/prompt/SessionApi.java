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
package aprilflow.sdk.prompt;

import aprilflow.sdk.AprilFlowException;
import aprilflow.sdk.http.ApiPath;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.json.JsonSerializer;
import aprilflow.sdk.notification.Notification;
import aprilflow.sdk.notification.NotificationApi;
import aprilflow.sdk.notification.NotificationSubscription;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class SessionApi
{
    private static final String PROMPT_ACTION = "prompt.process";
    private static final String PROMPT_OBJECT_TYPE = "prompt";

    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;
    private final NotificationApi notificationApi;

    public SessionApi(AprilFlowHttpClient httpClient,
            JsonSerializer jsonSerializer,
            NotificationApi notificationApi)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;

        this.notificationApi = notificationApi;
    }

    public CreateSessionResult create(PromptRequest request)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post("/prompt/v1/pub/sessions")
                    .json(jsonSerializer.serialize(request))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), CreateSessionResult.class);
    }

    public CreateSessionResult createAndWait(PromptRequest request, PromptWaitOptions waitOptions)
    {
        CreateSessionResult result;
        Prompt prompt;

        result = create(request);

        prompt = waitForCompletion(result.getPrompt(), waitOptions);

        return new CreateSessionResult(result.getSession(), prompt);
    }

    public Prompt update(String sessionId, PromptRequest request)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/prompt/v1/pub/sessions/" + ApiPath.segment(sessionId))
                    .json(jsonSerializer.serialize(request))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Prompt.class);
    }

    public Prompt updateAndWait(String sessionId, PromptRequest request, PromptWaitOptions waitOptions)
    {
        Prompt prompt;

        prompt = update(sessionId, request);

        return waitForCompletion(prompt, waitOptions);
    }

    public Session updateTitle(String sessionId, String title)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put("/prompt/v1/pub/sessions/" + ApiPath.segment(sessionId) + "/title")
                    .plainText(title)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Session.class);
    }

    public Session duplicate(String sessionId, String title)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post("/prompt/v1/pub/sessions/" + ApiPath.segment(sessionId) + "/duplicate")
                    .plainText(title)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Session.class);
    }

    public int count()
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get("/prompt/v1/pub/sessions/count")
                    .build()
                    .toHttpRequest()
            )
        );

        return Integer.parseInt(response.body());
    }

    public List<Session> list(SessionListRequest request)
    {
        HttpResponse response;
        String path;

        path = ApiPath.of("/prompt/v1/pub/sessions")
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
            new TypeReference<List<Session>>()
            {
            }
        );
    }

    public void delete(String sessionId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete("/prompt/v1/pub/sessions/" + ApiPath.segment(sessionId))
                    .build()
                    .toHttpRequest()
            )
        );
    }

    private Prompt waitForCompletion(Prompt prompt, PromptWaitOptions waitOptions)
    {
        AtomicReference<Prompt> promptResult;
        AtomicReference<Throwable> errorResult;
        CountDownLatch latch;
        NotificationSubscription subscription;
        PromptWaitOptions options;
        boolean completed;

        options = resolveWaitOptions(waitOptions);

        if (isTerminal(prompt.getStatus(), options))
        {
            return prompt;
        }

        promptResult = new AtomicReference<>();
        errorResult = new AtomicReference<>();
        latch = new CountDownLatch(1);

        subscription = notificationApi.listen(
            List.of(PROMPT_OBJECT_TYPE),
            notification -> {
                Prompt notifiedPrompt;

                if (!isPromptNotification(notification))
                {
                    return;
                }

                notifiedPrompt = toPrompt(notification);

                if (notifiedPrompt == null)
                {
                    return;
                }

                if (!prompt.getId().equals(notifiedPrompt.getId()))
                {
                    return;
                }

                if (isTerminal(notifiedPrompt.getStatus(), options))
                {
                    promptResult.set(notifiedPrompt);

                    latch.countDown();
                }
            },
            error -> {
                errorResult.set(error);

                latch.countDown();
            }
        );

        try
        {
            completed = latch.await(options.timeout().toMillis(), TimeUnit.MILLISECONDS);

            if (!completed)
            {
                throw new AprilFlowException("Timed out waiting for prompt " + prompt.getId() + " to complete");
            }

            if (errorResult.get() != null)
            {
                throw new AprilFlowException("Error while waiting for prompt " + prompt.getId() + " to complete", errorResult.get());
            }

            if (promptResult.get() == null)
            {
                throw new AprilFlowException("Prompt " + prompt.getId() + " completed without a prompt payload");
            }

            return promptResult.get();
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();

            throw new AprilFlowException("Interrupted while waiting for prompt " + prompt.getId() + " to complete", exception);
        }
        finally
        {
            subscription.close();
        }
    }

    private PromptWaitOptions resolveWaitOptions(PromptWaitOptions waitOptions)
    {
        if (waitOptions == null)
        {
            return PromptWaitOptions.defaultOptions();
        }

        return waitOptions;
    }

    private boolean isPromptNotification(Notification notification)
    {
        if (notification == null)
        {
            return false;
        }

        if (!PROMPT_ACTION.equals(notification.getAction()))
        {
            return false;
        }

        return PROMPT_OBJECT_TYPE.equals(notification.getObjectType());
    }

    private Prompt toPrompt(Notification notification)
    {
        if (notification == null || notification.getObject() == null)
        {
            return null;
        }

        return jsonSerializer.deserialize(notification.getObject().toString(), Prompt.class);
    }

    private boolean isTerminal(PromptStatus status, PromptWaitOptions waitOptions)
    {
        if (status == null)
        {
            return false;
        }

        return waitOptions.terminalStatuses().contains(status);
    }
}
