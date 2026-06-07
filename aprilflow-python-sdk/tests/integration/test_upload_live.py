# Copyright 2026 April Software
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

from __future__ import annotations

from io import BytesIO

import pytest

from aprilflow import CollectionVisibility, UploadWaitOptions

pytestmark = pytest.mark.integration


def test_upload_bytes_file_and_stream_wait(client, unique_name, sample_file):
    collection = client.collection.create(
        name=unique_name,
        description="Upload integration test collection",
        visibility=CollectionVisibility.PRIVATE,
    )
    uploads = []
    try:
        wait_options = UploadWaitOptions(timeout=300)
        uploads.append(
            client.upload.upload_bytes_and_wait(
                collection.id,
                b"Bytes uploaded by the Python SDK integration tests.",
                "bytes.txt",
                wait_options=wait_options,
            )
        )
        uploads.append(
            client.upload.upload_file_and_wait(
                collection.id,
                sample_file,
                wait_options=wait_options,
            )
        )
        uploads.append(
            client.upload.upload_stream_and_wait(
                collection.id,
                BytesIO(b"Stream uploaded by the Python SDK integration tests."),
                "stream.txt",
                wait_options=wait_options,
            )
        )
        listed = client.upload.list(collection_id=collection.id, first_result=0, max_result=20)
        assert len(listed) >= 3
        assert client.upload.count(collection_id=collection.id) >= 3
    finally:
        for upload in uploads:
            if upload and upload.id:
                try:
                    client.upload.delete(collection.id, upload.id, delete_document=True)
                except Exception:
                    pass
        client.collection.delete(collection.id)
