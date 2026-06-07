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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpRequest
{
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final HttpBody body;

    public HttpRequest(HttpMethod method, String path)
    {
        this(method, path, Collections.emptyMap(), (HttpBody) null);
    }

    public HttpRequest(HttpMethod method, String path, Map<String, String> headers, String body)
    {
        this(method,
            path,
            headers,
            body == null ? null : HttpBody.text(body, headers == null ? null : headers.get("Content-Type")));
    }

    public HttpRequest(HttpMethod method, String path, Map<String, String> headers, HttpBody body)
    {
        if (headers == null)
        {
            headers = Collections.emptyMap();
        }

        this.method = method;

        this.path = path;

        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));

        this.body = body;
    }

    public HttpMethod method()
    {
        return method;
    }

    public String path()
    {
        return path;
    }

    public Map<String, String> headers()
    {
        return headers;
    }

    public HttpBody body()
    {
        return body;
    }

    public String textBody()
    {
        if (body == null || !body.isText())
        {
            return null;
        }

        return body.text();
    }
}
