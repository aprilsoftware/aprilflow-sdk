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
import aprilflow.sdk.document.DocumentItem;
import aprilflow.sdk.document.DocumentSearchRequest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class DocumentTest extends BaseTest
{
    @Test
    void testDocument()
    {
        AprilFlowClient client;
        Collection collection;

        client = initTest();

        collection = null;

        try
        {
            collection = client.collection().create(
                CreateCollectionRequest.create()
                    .name(getTestName("java-sdk-it-document-empty"))
                    .description("Temporary collection created by AprilFlow Java SDK document test")
                    .visibility(CollectionVisibility.Private)
            );

            assertThat(collection).isNotNull();
            assertThat(collection.getId()).isNotBlank();

            List<DocumentItem> documents;

            documents = client.document().search(
                DocumentSearchRequest.create()
                    .collectionId(collection.getId())
                    .text("")
                    .maxResult(20)
            );

            assertThat(documents).isNotNull();
            assertThat(documents).isEmpty();
        }
        finally
        {
            if (client != null && collection != null && collection.getId() != null)
            {
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
}
