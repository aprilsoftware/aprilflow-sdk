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
package aprilflow.sdk.tests;

import aprilflow.sdk.AprilFlowClient;
import aprilflow.sdk.collection.Collection;
import aprilflow.sdk.collection.CollectionVisibility;
import aprilflow.sdk.collection.CreateCollectionRequest;
import aprilflow.sdk.upload.Upload;
import aprilflow.sdk.upload.UploadBytesRequest;
import aprilflow.sdk.upload.UploadFileRequest;
import aprilflow.sdk.upload.UploadListRequest;
import aprilflow.sdk.upload.UploadStatus;
import aprilflow.sdk.upload.UploadStreamRequest;
import aprilflow.sdk.upload.UploadWaitOptions;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class UploadTest extends BaseTest
{
    @Test
    public void testUpload1()
    {
        AprilFlowClient client;
        Collection collection;
        Upload upload;

        client = initTest();

        collection = null;
        upload = null;

        try
        {
            collection = createTemporaryCollection(client, "java-sdk-it-upload-bytes-wait");

            byte[] bytes;

            bytes = """
                Hello from AprilFlow Java SDK upload bytes notification test.

                This file is used to verify upload bytes processing through notifications.
                """.getBytes(StandardCharsets.UTF_8);

            upload = client.upload().uploadBytesAndWait(
                UploadBytesRequest.create()
                    .collectionId(collection.getId())
                    .fileName("upload-bytes-notification-test.txt")
                    .contentType("text/plain")
                    .bytes(bytes),
                defaultUploadWaitOptions()
            );

            assertProcessedUpload(
                upload,
                collection.getId(),
                "upload-bytes-notification-test.txt"
            );

            assertUploadList(client, collection, upload);
        }
        finally
        {
            cleanup(client, collection, upload);
        }
    }

    @Test
    public void testUpload2() throws Exception
    {
        AprilFlowClient client;
        Collection collection;
        Upload upload;
        Path file;

        client = initTest();

        collection = null;
        upload = null;
        file = null;

        try
        {
            collection = createTemporaryCollection(client, "java-sdk-it-upload-file-wait");

            file = Files.createTempFile("aprilflow-java-sdk-upload-file-", ".txt");

            Files.writeString(
                file,
                """
                Hello from AprilFlow Java SDK upload file notification test.

                This file is used to verify upload file processing through notifications.
                """,
                StandardCharsets.UTF_8
            );

            upload = client.upload().uploadFileAndWait(
                UploadFileRequest.create()
                    .collectionId(collection.getId())
                    .file(file)
                    .fileName("upload-file-notification-test.txt")
                    .contentType("text/plain"),
                defaultUploadWaitOptions()
            );

            assertProcessedUpload(
                upload,
                collection.getId(),
                "upload-file-notification-test.txt"
            );

            assertUploadList(client, collection, upload);
        }
        finally
        {
            cleanup(client, collection, upload);

            if (file != null)
            {
                try
                {
                    Files.deleteIfExists(file);
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }

    @Test
    public void testUpload3()
    {
        AprilFlowClient client;
        Collection collection;
        Upload upload;

        client = initTest();

        collection = null;
        upload = null;

        try
        {
            collection = createTemporaryCollection(client, "java-sdk-it-upload-stream-wait");

            byte[] bytes;

            bytes = """
                Hello from AprilFlow Java SDK upload stream notification test.

                This file is used to verify upload stream processing through notifications.
                """.getBytes(StandardCharsets.UTF_8);

            upload = client.upload().uploadStreamAndWait(
                UploadStreamRequest.create()
                    .collectionId(collection.getId())
                    .fileName("upload-stream-notification-test.txt")
                    .contentType("text/plain")
                    .inputStream(() -> new ByteArrayInputStream(bytes)),
                defaultUploadWaitOptions()
            );

            assertProcessedUpload(
                upload,
                collection.getId(),
                "upload-stream-notification-test.txt"
            );

            assertUploadList(client, collection, upload);
        }
        finally
        {
            cleanup(client, collection, upload);
        }
    }

    private Collection createTemporaryCollection(AprilFlowClient client, String name)
    {
        Collection collection;

        collection = client.collection().create(
            CreateCollectionRequest.create()
                .name(getTestName(name))
                .description("Temporary collection created by AprilFlow Java SDK upload notification test")
                .visibility(CollectionVisibility.Private)
        );

        assertThat(collection).isNotNull();
        assertThat(collection.getId()).isNotBlank();

        return collection;
    }

    private UploadWaitOptions defaultUploadWaitOptions()
    {
        return new UploadWaitOptions(
            Duration.ofMinutes(3),
            List.of(
                UploadStatus.Processed,
                UploadStatus.Ignored,
                UploadStatus.OnError,
                UploadStatus.QuotaExceeded
            )
        );
    }

    private void assertProcessedUpload(Upload upload, String collectionId, String fileName)
    {
        assertThat(upload).isNotNull();
        assertThat(upload.getId()).isNotBlank();
        assertThat(upload.getCollectionId()).isEqualTo(collectionId);
        assertThat(upload.getFileName()).isEqualTo(fileName);
    }

    private void assertUploadList(AprilFlowClient client,
            Collection collection,
            Upload upload)
    {
        List<Upload> uploads;
        int count;

        uploads = client.upload().list(
            UploadListRequest.create()
                .collectionId(collection.getId())
                .firstResult(0)
                .maxResult(20)
        );

        assertThat(uploads)
            .extracting(Upload::getId)
            .contains(upload.getId());

        count = client.upload().count(
            UploadListRequest.create()
                .collectionId(collection.getId())
        );

        assertThat(count).isGreaterThanOrEqualTo(1);
    }

    private void cleanup(AprilFlowClient client, Collection collection, Upload upload)
    {
        if (client != null && collection != null && collection.getId() != null)
        {
            if (upload != null && upload.getId() != null)
            {
                try
                {
                    client.upload().delete(collection.getId(), upload.getId(), true);
                }
                catch (RuntimeException ignored)
                {
                }
            }

            try
            {
                client.collection().delete(collection.getId());
            }
            catch (RuntimeException ignored)
            {
            }
        }
    }
}
