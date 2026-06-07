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
package aprilflow.sdk.upload;

import java.nio.file.Path;

public final class UploadFileRequest
{
    private String collectionId;
    private Path file;
    private String fileName;
    private String contentType;

    private UploadFileRequest()
    {
    }

    public static UploadFileRequest create()
    {
        return new UploadFileRequest();
    }

    public UploadFileRequest collectionId(String collectionId)
    {
        this.collectionId = collectionId;

        return this;
    }

    public UploadFileRequest file(Path file)
    {
        this.file = file;

        return this;
    }

    public UploadFileRequest fileName(String fileName)
    {
        this.fileName = fileName;

        return this;
    }

    public UploadFileRequest contentType(String contentType)
    {
        this.contentType = contentType;

        return this;
    }

    public String collectionId()
    {
        return collectionId;
    }

    public Path file()
    {
        return file;
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
