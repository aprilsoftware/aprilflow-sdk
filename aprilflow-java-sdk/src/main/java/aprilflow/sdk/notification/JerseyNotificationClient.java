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
package aprilflow.sdk.notification;

import aprilflow.sdk.AprilFlowException;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;

import org.glassfish.jersey.jackson.JacksonFeature;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class JerseyNotificationClient implements AprilFlowNotificationClient
{
    private static final String NOTIFICATION_EVENT_NAME = "notification";

    private final String baseUrl;
    private final Client client;

    public JerseyNotificationClient(String baseUrl)
    {
        this.baseUrl = baseUrl;

        this.client = ClientBuilder.newBuilder()
            .register(JacksonFeature.class)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public NotificationSubscription listen(NotificationRequest request)
    {
        SseEventSource eventSource;
        WebTarget target;

        target = targetFor(request);

        eventSource = SseEventSource.target(target)
            .reconnectingEvery(1, TimeUnit.SECONDS)
            .build();

        eventSource.register(
            event -> onEvent(event, request),
            error -> onError(error, request),
            () -> {
            }
        );

        try
        {
            eventSource.open();
        }
        catch (RuntimeException exception)
        {
            eventSource.close();

            throw new AprilFlowException("Unable to open notification stream", exception);
        }

        return new NotificationSubscription(eventSource);
    }

    private void onEvent(InboundSseEvent event, NotificationRequest request)
    {
        Notification notification;
        String eventName;

        try
        {
            eventName = event.getName();

            if (eventName != null && !NOTIFICATION_EVENT_NAME.equals(eventName))
            {
                return;
            }

            notification = event.readData(Notification.class, MediaType.APPLICATION_JSON_TYPE);

            request.listener().onNotification(notification);
        }
        catch (Throwable error)
        {
            onError(error, request);
        }
    }

    private void onError(Throwable error, NotificationRequest request)
    {
        if (request.errorListener() != null)
        {
            request.errorListener().onError(error);
        }
    }

    private WebTarget targetFor(NotificationRequest request)
    {
        WebTarget target;

        target = client.target(toAbsoluteUrl(request.path()));

        if (!request.headers().isEmpty())
        {
            target = target.register(new HeaderFilter(request.headers()));
        }

        return target;
    }

    private String toAbsoluteUrl(String path)
    {
        String normalizedPath;
        String normalizedUrl;

        normalizedUrl = this.baseUrl;

        if (normalizedUrl.endsWith("/"))
        {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }

        normalizedPath = normalizePath(path);

        return normalizedUrl + "/" + normalizedPath;
    }

    private String normalizePath(String path)
    {
        if (path.startsWith("/"))
        {
            return path.substring(1);
        }

        return path;
    }

    private static final class HeaderFilter implements ClientRequestFilter
    {
        private final Map<String, String> headers;

        private HeaderFilter(Map<String, String> headers)
        {
            this.headers = new LinkedHashMap<>(headers);
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                requestContext.getHeaders().putSingle(header.getKey(), header.getValue());
            }
        }
    }
}
