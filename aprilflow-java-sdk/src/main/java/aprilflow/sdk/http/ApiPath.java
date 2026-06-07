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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ApiPath
{
    private final String path;
    private final List<QueryParameter> queryParameters;

    private ApiPath(String path)
    {
        this.path = path;

        this.queryParameters = new ArrayList<>();
    }

    public static ApiPath of(String path)
    {
        return new ApiPath(path);
    }

    public static String segment(String value)
    {
        return encode(value).replace("+", "%20");
    }

    public ApiPath query(String name, String value)
    {
        if (value != null)
        {
            queryParameters.add(new QueryParameter(name, value));
        }

        return this;
    }

    public ApiPath query(String name, Integer value)
    {
        if (value != null)
        {
            query(name, String.valueOf(value));
        }

        return this;
    }

    public ApiPath query(String name, Long value)
    {
        if (value != null)
        {
            query(name, String.valueOf(value));
        }

        return this;
    }

    public ApiPath query(String name, Boolean value)
    {
        if (value != null)
        {
            query(name, String.valueOf(value));
        }

        return this;
    }

    public String build()
    {
        StringBuilder builder;

        if (queryParameters.isEmpty())
        {
            return path;
        }

        builder = new StringBuilder(path);

        builder.append("?");

        boolean first = true;

        for (QueryParameter queryParameter : queryParameters)
        {
            if (!first)
            {
                builder.append("&");
            }

            builder.append(encode(queryParameter.name()));
            builder.append("=");
            builder.append(encode(queryParameter.value()));

            first = false;
        }

        return builder.toString();
    }

    private static String encode(String value)
    {
        try
        {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException exception)
        {
            throw new IllegalStateException("UTF-8 is not supported", exception);
        }
    }

    private static final class QueryParameter
    {
        private final String name;
        private final String value;

        private QueryParameter(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        private String name()
        {
            return name;
        }

        private String value()
        {
            return value;
        }
    }
}
