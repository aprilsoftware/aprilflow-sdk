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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NotificationRequest
{
    private final String path;
    private final Map<String, String> headers;
    private final NotificationListener listener;
    private final NotificationErrorListener errorListener;

    private NotificationRequest(String path,
            Map<String, String> headers,
            NotificationListener listener,
            NotificationErrorListener errorListener)
    {
        this.path = path;

        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));

        this.listener = listener;

        this.errorListener = errorListener;
    }

    public static NotificationRequest of(String path,
            NotificationListener listener,
            NotificationErrorListener errorListener)
    {
        return new NotificationRequest(path,
                Collections.emptyMap(),
                listener,
                errorListener);
    }

    public NotificationRequest header(String name, String value)
    {
        Map<String, String> headers;

        headers = new LinkedHashMap<>(this.headers);

        if (value != null)
        {
            headers.put(name, value);
        }

        return new NotificationRequest(path,
                headers,
                listener,
                errorListener);
    }

    public String path()
    {
        return path;
    }

    public Map<String, String> headers()
    {
        return headers;
    }

    public NotificationListener listener()
    {
        return listener;
    }

    public NotificationErrorListener errorListener()
    {
        return errorListener;
    }
}
