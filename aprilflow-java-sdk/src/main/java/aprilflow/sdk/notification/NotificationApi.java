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

import aprilflow.sdk.auth.TokenProvider;
import aprilflow.sdk.http.ApiPath;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import java.util.Collections;
import java.util.List;

public final class NotificationApi
{
    private final AprilFlowNotificationClient notificationClient;

    public NotificationApi(AprilFlowNotificationClient notificationClient)
    {
        this.notificationClient = notificationClient;
    }

    public NotificationApi(String baseUrl, TokenProvider tokenProvider)
    {
        this(new AuthenticatedNotificationClient(
            new JerseyNotificationClient(baseUrl),
            tokenProvider
        ));
    }

    public NotificationSubscription listen(NotificationListener listener)
    {
        return listen(Collections.emptyList(), listener, null);
    }

    public NotificationSubscription listen(NotificationListener listener,
            NotificationErrorListener errorListener)
    {
        return listen(Collections.emptyList(), listener, errorListener);
    }

    public NotificationSubscription listen(List<String> objectTypes,
            NotificationListener listener)
    {
        return listen(objectTypes, listener, null);
    }

    public NotificationSubscription listen(List<String> objectTypes,
            NotificationListener listener,
            NotificationErrorListener errorListener)
    {
        NotificationRequest request;
        String path;

        path = toNotificationPath(objectTypes);

        request = NotificationRequest.of(path, listener, errorListener)
            .header(HttpHeaders.ACCEPT, MediaType.SERVER_SENT_EVENTS);

        return notificationClient.listen(request);
    }

    private String toNotificationPath(List<String> objectTypes)
    {
        ApiPath path;

        path = ApiPath.of("/message/v1/pub/notifications");

        if (objectTypes == null)
        {
            return path.build();
        }

        for (String objectType : objectTypes)
        {
            if (objectType != null && !objectType.isBlank())
            {
                path.query("objectType", objectType);
            }
        }

        return path.build();
    }
}
