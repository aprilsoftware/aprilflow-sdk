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
import java.util.Arrays;
import java.util.function.Supplier;

public final class HttpBody
{
    private final String text;
    private final Path file;
    private final byte[] bytes;
    private final Supplier<InputStream> inputStreamSupplier;
    private final String fileName;
    private final String contentType;

    private HttpBody(String text, 
            Path file,
            byte[] bytes,
            Supplier<InputStream> inputStreamSupplier,
            String fileName,
            String contentType)
    {
        this.text = text;

        this.file = file;

        if (bytes == null)
        {
            this.bytes = null;
        }
        else
        {
            this.bytes = Arrays.copyOf(bytes, bytes.length);
        }

        this.inputStreamSupplier = inputStreamSupplier;

        this.fileName = fileName;

        this.contentType = contentType;
    }

    public static HttpBody text(String text, String contentType)
    {
        return new HttpBody(text, null, null, null, null, contentType);
    }

    public static HttpBody file(Path file, String fileName, String contentType)
    {
        return new HttpBody(null, file, null, null, fileName, contentType);
    }

    public static HttpBody bytes(byte[] bytes, String fileName, String contentType)
    {
        return new HttpBody(null, null, bytes, null, fileName, contentType);
    }

    public static HttpBody stream(Supplier<InputStream> inputStreamSupplier, String fileName, String contentType)
    {
        return new HttpBody(null, null, null, inputStreamSupplier, fileName, contentType);
    }

    public boolean isText()
    {
        return text != null;
    }

    public boolean isFile()
    {
        return file != null;
    }

    public boolean isBytes()
    {
        return bytes != null;
    }

    public boolean isStream()
    {
        return inputStreamSupplier != null;
    }

    public String text()
    {
        return text;
    }

    public Path file()
    {
        return file;
    }

    public byte[] bytes()
    {
        if (bytes == null)
        {
            return null;
        }

        return Arrays.copyOf(bytes, bytes.length);
    }

    public InputStream openStream()
    {
        if (inputStreamSupplier == null)
        {
            return null;
        }

        return inputStreamSupplier.get();
    }

    public String fileName()
    {
        return fileName;
    }

    public String contentType()
    {
        return contentType;
    }
}
