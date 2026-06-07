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

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ApiRequest
{
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final HttpBody body;

    private ApiRequest(Builder builder)
    {
        this.method = builder.method;

        this.path = builder.path;

        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers));

        this.body = builder.body;
    }

    public HttpRequest toHttpRequest()
    {
        return new HttpRequest(method, path, headers, body);
    }

    public static Builder get(String path)
    {
        return new Builder(HttpMethod.GET, path);
    }

    public static Builder post(String path)
    {
        return new Builder(HttpMethod.POST, path);
    }

    public static Builder put(String path)
    {
        return new Builder(HttpMethod.PUT, path);
    }

    public static Builder patch(String path)
    {
        return new Builder(HttpMethod.PATCH, path);
    }

    public static Builder delete(String path)
    {
        return new Builder(HttpMethod.DELETE, path);
    }

    public static final class Builder
    {
        private final HttpMethod method;
        private final String path;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private HttpBody body;

        private Builder(HttpMethod method, String path)
        {
            this.method = method;

            this.path = path;
        }

        public Builder header(String name, String value)
        {
            if (value != null)
            {
                headers.put(name, value);
            }

            return this;
        }

        public Builder json(String body)
        {
            this.body = HttpBody.text(body, "application/json");

            this.header("Content-Type", "application/json");

            return this;
        }

        public Builder body(String body)
        {
            this.body = HttpBody.text(body, null);

            return this;
        }

        public Builder plainText(String body)
        {
            this.body = HttpBody.text(body, "text/plain");

            this.header("Content-Type", "text/plain");

            return this;
        }

        public Builder file(Path file, String fileName, String contentType)
        {
            this.body = HttpBody.file(file, fileName, contentType);

            return this;
        }

        public Builder bytes(byte[] bytes, String fileName, String contentType)
        {
            this.body = HttpBody.bytes(bytes, fileName, contentType);

            return this;
        }

        public Builder stream(Supplier<InputStream> inputStreamSupplier, String fileName, String contentType)
        {
            this.body = HttpBody.stream(inputStreamSupplier, fileName, contentType);

            return this;
        }

        public ApiRequest build()
        {
            return new ApiRequest(this);
        }
    }
}
