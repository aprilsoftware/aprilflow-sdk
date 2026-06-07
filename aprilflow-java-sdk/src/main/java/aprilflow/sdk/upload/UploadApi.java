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

import aprilflow.sdk.AprilFlowException;
import aprilflow.sdk.http.ApiPath;
import aprilflow.sdk.http.ApiRequest;
import aprilflow.sdk.http.ApiResponseHandler;
import aprilflow.sdk.http.AprilFlowHttpClient;
import aprilflow.sdk.http.HttpResponse;
import aprilflow.sdk.json.JsonSerializer;
import aprilflow.sdk.notification.Notification;
import aprilflow.sdk.notification.NotificationApi;
import aprilflow.sdk.notification.NotificationErrorListener;
import aprilflow.sdk.notification.NotificationListener;
import aprilflow.sdk.notification.NotificationSubscription;

import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class UploadApi
{
    private static final String UPLOAD_ACTION = "upload.process";
    private static final String UPLOAD_OBJECT_TYPE = "collection.upload";

    private final AprilFlowHttpClient httpClient;
    private final JsonSerializer jsonSerializer;
    private final NotificationApi notificationApi;

    public UploadApi(AprilFlowHttpClient httpClient, JsonSerializer jsonSerializer, NotificationApi notificationApi)
    {
        this.httpClient = httpClient;

        this.jsonSerializer = jsonSerializer;

        this.notificationApi = notificationApi;
    }

    public List<Upload> list(UploadListRequest request)
    {
        HttpResponse response;
        ApiPath path;

        path = ApiPath.of("/collection/v1/pub/" + ApiPath.segment(request.collectionId()) + "/uploads")
            .query("search", request.search())
            .query("firstResult", request.firstResult())
            .query("maxResult", request.maxResult());

        for (UploadStatus status : request.statuses())
        {
            path.query("status", status.getName());
        }

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(path.build())
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<Upload>>()
            {
            }
        );
    }

    public int count(UploadListRequest request)
    {
        HttpResponse response;
        ApiPath path;

        path = ApiPath.of("/collection/v1/pub/" + ApiPath.segment(request.collectionId()) + "/uploads/count")
            .query("search", request.search());

        for (UploadStatus status : request.statuses())
        {
            path.query("status", status.getName());
        }

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.get(path.build())
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Integer.class);
    }

    public List<Upload> batch(UploadBatchRequest request)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post(
                    "/collection/v1/pub/"
                        + ApiPath.segment(request.collectionId())
                        + "/uploads/ids"
                )
                    .json(jsonSerializer.serialize(request.uploadIds()))
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(
            response.body(),
            new TypeReference<List<Upload>>()
            {
            }
        );
    }

    public Upload uploadFile(UploadFileRequest request)
    {
        HttpResponse response;
        String contentType;
        String fileName;
        String path;

        fileName = request.fileName();

        if (fileName == null || fileName.isBlank())
        {
            fileName = request.file().getFileName().toString();
        }

        contentType = request.contentType();

        if (contentType == null || contentType.isBlank())
        {
            contentType = detectContentType(request.file());
        }

        path = ApiPath.of("/collection/v1/pub/" + ApiPath.segment(request.collectionId()) + "/uploads").build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post(path)
                    .file(request.file(), fileName, contentType)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Upload.class);
    }

    public Upload uploadBytes(UploadBytesRequest request)
    {
        HttpResponse response;
        String contentType;
        String path;

        contentType = request.contentType();

        if (contentType == null || contentType.isBlank())
        {
            contentType = "application/octet-stream";
        }

        path = ApiPath.of("/collection/v1/pub/" + ApiPath.segment(request.collectionId()) + "/uploads").build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post(path)
                    .bytes(request.bytes(), request.fileName(), contentType)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Upload.class);
    }

    public Upload uploadStream(UploadStreamRequest request)
    {
        HttpResponse response;
        String contentType;
        String path;

        contentType = request.contentType();

        if (contentType == null || contentType.isBlank())
        {
            contentType = "application/octet-stream";
        }

        path = ApiPath.of("/collection/v1/pub/" + ApiPath.segment(request.collectionId()) + "/uploads").build();

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.post(path)
                    .stream(request.inputStreamSupplier(), request.fileName(), contentType)
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Upload.class);
    }

    public void delete(String collectionId, String uploadId, boolean deleteDocument)
    {
        String path;

        path = ApiPath.of(
                "/collection/v1/pub/"
                    + ApiPath.segment(collectionId)
                    + "/uploads/"
                    + ApiPath.segment(uploadId)
            )
            .query("deleteDocument", deleteDocument)
            .build();

        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete(path)
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public void deleteDocument(String collectionId, String uploadId)
    {
        ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.delete(
                    "/collection/v1/pub/"
                        + ApiPath.segment(collectionId)
                        + "/uploads/"
                        + ApiPath.segment(uploadId)
                        + "/document"
                )
                    .build()
                    .toHttpRequest()
            )
        );
    }

    public Upload cancel(String collectionId, String uploadId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put(
                    "/collection/v1/pub/"
                        + ApiPath.segment(collectionId)
                        + "/uploads/"
                        + ApiPath.segment(uploadId)
                        + "/process/cancel"
                )
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Upload.class);
    }

    public Upload restart(String collectionId, String uploadId)
    {
        HttpResponse response;

        response = ApiResponseHandler.requireSuccess(
            httpClient.execute(
                ApiRequest.put(
                    "/collection/v1/pub/"
                        + ApiPath.segment(collectionId)
                        + "/uploads/"
                        + ApiPath.segment(uploadId)
                        + "/process/restart"
                )
                    .build()
                    .toHttpRequest()
            )
        );

        return jsonSerializer.deserialize(response.body(), Upload.class);
    }

    public NotificationSubscription watch(String collectionId, String uploadId, NotificationListener listener)
    {
        return watch(collectionId, uploadId, listener, null);
    }

    public NotificationSubscription watch(String collectionId,
            String uploadId,
            NotificationListener listener,
            NotificationErrorListener errorListener)
    {
        return notificationApi.listen(
            List.of(UPLOAD_OBJECT_TYPE),
            notification -> {
                Upload upload;

                if (!isUploadNotification(notification))
                {
                    return;
                }

                upload = toUpload(notification);

                if (upload == null)
                {
                    return;
                }

                if (!collectionId.equals(upload.getCollectionId()))
                {
                    return;
                }

                if (!uploadId.equals(upload.getId()))
                {
                    return;
                }

                listener.onNotification(notification);
            },
            errorListener
        );
    }

    public Upload uploadFileAndWait(UploadFileRequest request, UploadWaitOptions waitOptions)
    {
        Upload upload;

        upload = uploadFile(request);

        return waitForUpload(
            upload,
            waitOptions
        );
    }

    public Upload uploadBytesAndWait(UploadBytesRequest request, UploadWaitOptions waitOptions)
    {
        Upload upload;

        upload = uploadBytes(request);

        return waitForUpload(
            upload,
            waitOptions
        );
    }

    public Upload uploadStreamAndWait(UploadStreamRequest request, UploadWaitOptions waitOptions)
    {
        Upload upload;

        upload = uploadStream(request);

        return waitForUpload(
            upload,
            waitOptions
        );
    }

    private Upload waitForUpload(Upload upload, UploadWaitOptions waitOptions)
    {
        AtomicReference<Upload> uploadResult;
        AtomicReference<Throwable> errorResult;
        CountDownLatch latch;
        NotificationSubscription subscription;
        UploadWaitOptions options;
        String collectionId;
        boolean completed;

        options = resolveWaitOptions(waitOptions);

        collectionId = upload.getCollectionId();

        if (isTerminal(upload.getStatus(), options))
        {
            return upload;
        }

        uploadResult = new AtomicReference<>();
        errorResult = new AtomicReference<>();
        latch = new CountDownLatch(1);

        subscription = watch(
            collectionId,
            upload.getId(),
            notification -> {
                Upload notifiedUpload;

                notifiedUpload = toUpload(notification);

                if (notifiedUpload == null)
                {
                    return;
                }

                if (isTerminal(notifiedUpload.getStatus(), options))
                {
                    uploadResult.set(notifiedUpload);

                    latch.countDown();
                }
            },
            error -> {
                errorResult.set(error);

                latch.countDown();
            }
        );

        try
        {
            completed = latch.await(options.timeout().toMillis(), TimeUnit.MILLISECONDS);

            if (!completed)
            {
                throw new AprilFlowException("Timed out waiting for upload " + upload.getId() + " to complete");
            }

            if (errorResult.get() != null)
            {
                throw new AprilFlowException("Error while waiting for upload " + upload.getId() + " to complete", errorResult.get());
            }

            if (uploadResult.get() == null)
            {
                throw new AprilFlowException("Upload " + upload.getId() + " completed without an upload payload");
            }

            return uploadResult.get();
        }
        catch (InterruptedException exception)
        {
            Thread.currentThread().interrupt();

            throw new AprilFlowException("Interrupted while waiting for upload " + upload.getId() + " to complete", exception);
        }
        finally
        {
            subscription.close();
        }
    }

    private UploadWaitOptions resolveWaitOptions(UploadWaitOptions waitOptions)
    {
        if (waitOptions == null)
        {
            return UploadWaitOptions.defaultOptions();
        }

        return waitOptions;
    }

    private boolean isUploadNotification(Notification notification)
    {
        if (notification == null)
        {
            return false;
        }

        if (!UPLOAD_ACTION.equals(notification.getAction()))
        {
            return false;
        }

        return UPLOAD_OBJECT_TYPE.equals(notification.getObjectType());
    }

    private Upload toUpload(Notification notification)
    {
        if (notification == null || notification.getObject() == null)
        {
            return null;
        }

        return jsonSerializer.deserialize(notification.getObject().toString(), Upload.class);
    }

    private boolean isTerminal(UploadStatus status, UploadWaitOptions waitOptions)
    {
        if (status == null)
        {
            return false;
        }

        return waitOptions.terminalStatuses().contains(status);
    }

    private String detectContentType(Path file)
    {
        try
        {
            String contentType = Files.probeContentType(file);

            if (contentType == null || contentType.isBlank())
            {
                return "application/octet-stream";
            }

            return contentType;
        }
        catch (Exception exception)
        {
            return "application/octet-stream";
        }
    }
}
