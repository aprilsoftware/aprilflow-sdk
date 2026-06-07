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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class JerseyHttpClient implements AprilFlowHttpClient
{
    private final String baseUrl;
    private final Client client;

    public JerseyHttpClient(String baseUrl)
    {
        this.baseUrl = baseUrl;

        Logger.getLogger("org.glassfish.jersey.client.internal.HttpUrlConnector")
            .setLevel(Level.SEVERE);

        Logger.getLogger("org.glassfish.jersey.client.ClientRuntime")
            .setLevel(Level.SEVERE);

        Logger.getLogger("org.glassfish.jersey.client")
            .setLevel(Level.SEVERE);

        this.client = ClientBuilder.newBuilder()
            .register(MultiPartFeature.class)
            .property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
            .build();
    }

    @Override
    public HttpResponse execute(HttpRequest request)
    {
        Response response;

        response = executeRequest(request);

        try
        {
            return toHttpResponse(response);
        }
        finally
        {
            response.close();
        }
    }

    private Response executeRequest(HttpRequest request)
    {
        Invocation.Builder builder;
        HttpBody body;

        builder = targetFor(request.path()).request();

        for (Map.Entry<String, String> header : request.headers().entrySet())
        {
            builder.header(header.getKey(), header.getValue());
        }

        body = request.body();

        if (body == null)
        {
            return builder.method(request.method().name());
        }

        if (body.isText())
        {
            return builder.method(request.method().name(),
                    Entity.entity(body.text(), mediaType(body)));
        }

        if (body.isFile())
        {
            return executeFileRequest(builder, request, body);
        }

        if (body.isBytes())
        {
            return executeBytesRequest(builder, request, body);
        }

        if (body.isStream())
        {
            return executeStreamRequest(builder, request, body);
        }

        return builder.method(request.method().name());
    }

    private Response executeFileRequest(Invocation.Builder builder, HttpRequest request, HttpBody body)
    {
        FormDataMultiPart multipart;
        StreamDataBodyPart filePart;
        InputStream inputStream;
        String contentType;
        String fileName;
        File file;

        file = body.file().toFile();

        fileName = body.fileName();

        if (fileName == null || fileName.isBlank())
        {
            fileName = file.getName();
        }

        contentType = body.contentType();

        if (contentType == null || contentType.isBlank())
        {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        try
        {
            inputStream = Files.newInputStream(body.file());

            filePart = new StreamDataBodyPart("file",
                    inputStream,
                    fileName,
                    MediaType.valueOf(contentType));

            multipart = new FormDataMultiPart();

            try
            {
                multipart.bodyPart(filePart);

                return builder.method(request.method().name(),
                        Entity.entity(multipart, multipart.getMediaType()));
            }
            finally
            {
                closeQuietly(multipart);
            }
        }
        catch (IOException exception)
        {
            throw new IllegalStateException("Unable to open file " + body.file(), exception);
        }
    }

    private Response executeBytesRequest(Invocation.Builder builder, HttpRequest request, HttpBody body)
    {
        FormDataMultiPart multipart;
        StreamDataBodyPart filePart;
        String contentType;
        String fileName;

        fileName = body.fileName();

        if (fileName == null || fileName.isBlank())
        {
            throw new IllegalArgumentException("fileName must not be blank");
        }

        contentType = body.contentType();

        if (contentType == null || contentType.isBlank())
        {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        filePart = new StreamDataBodyPart("file",
                new ByteArrayInputStream(body.bytes()),
                fileName,
                MediaType.valueOf(contentType));

        multipart = new FormDataMultiPart();

        try
        {
            multipart.bodyPart(filePart);

            return builder.method(request.method().name(),
                    Entity.entity(multipart, multipart.getMediaType()));
        }
        finally
        {
            closeQuietly(multipart);
        }
    }

    private Response executeStreamRequest(Invocation.Builder builder, HttpRequest request, HttpBody body)
    {
        FormDataMultiPart multipart;
        StreamDataBodyPart filePart;
        InputStream inputStream;
        String contentType;
        String fileName;

        fileName = body.fileName();

        if (fileName == null || fileName.isBlank())
        {
            throw new IllegalArgumentException("fileName must not be blank");
        }

        contentType = body.contentType();

        if (contentType == null || contentType.isBlank())
        {
            contentType = MediaType.APPLICATION_OCTET_STREAM;
        }

        inputStream = body.openStream();

        filePart = new StreamDataBodyPart("file",
                inputStream,
                fileName,
                MediaType.valueOf(contentType));

        multipart = new FormDataMultiPart();

        try
        {
            multipart.bodyPart(filePart);

            return builder.method(request.method().name(),
                    Entity.entity(multipart, multipart.getMediaType()));
        }
        finally
        {
            closeQuietly(multipart);
        }
    }

    private WebTarget targetFor(String path)
    {
        return client.target(toAbsoluteUrl(path));
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

    private void closeQuietly(FormDataMultiPart multipart)
    {
        if (multipart == null)
        {
            return;
        }

        try
        {
            multipart.close();
        }
        catch (IOException exception)
        {
        }
    }

    private String mediaType(HttpBody body)
    {
        String contentType;

        contentType = body.contentType();

        if (contentType == null || contentType.isBlank())
        {
            return MediaType.APPLICATION_JSON;
        }

        return contentType;
    }

    private HttpResponse toHttpResponse(Response response)
    {
        Map<String, String> headers;
        byte[] bodyBytes;

        headers = new LinkedHashMap<>();

        for (Map.Entry<String, java.util.List<Object>> entry : response.getHeaders().entrySet())
        {
            headers.put(entry.getKey(), joinHeaderValues(entry.getValue()));
        }

        bodyBytes = null;

        if (response.hasEntity())
        {
            bodyBytes = response.readEntity(byte[].class);
        }

        return new HttpResponse(response.getStatus(),
                headers,
                bodyBytes);
    }

    private String joinHeaderValues(java.util.List<Object> values)
    {
        StringBuilder builder;
        
        if (values == null || values.isEmpty())
        {
            return "";
        }

        builder = new StringBuilder();

        for (Object value : values)
        {
            if (builder.length() > 0)
            {
                builder.append(",");
            }

            builder.append(String.valueOf(value));
        }

        return builder.toString();
    }

    private String normalizePath(String path)
    {
        if (path.startsWith("/"))
        {
            return path.substring(1);
        }

        return path;
    }
}
