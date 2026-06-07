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
import aprilflow.sdk.collection.CollectionListRequest;
import aprilflow.sdk.collection.CollectionVisibility;
import aprilflow.sdk.collection.CreateCollectionRequest;
import aprilflow.sdk.collection.SetCollectionVisibilityRequest;
import aprilflow.sdk.collection.UpdateCollectionRequest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class CollectionTest extends BaseTest
{
    @Test
    public void testCollection()
    {
        AprilFlowClient client;
        Collection collection;
        boolean deleted;

        client = initTest();

        collection = null;

        deleted = false;

        try
        {
            String collectionName = getTestName("java-sdk-it-collection");

            collection = client.collection().create(
                CreateCollectionRequest.create()
                    .name(collectionName)
                    .description("Temporary collection created by AprilFlow Java SDK collection test")
                    .visibility(CollectionVisibility.Private)
            );

            assertThat(collection).isNotNull();
            assertThat(collection.getId()).isNotBlank();
            assertThat(collection.getName()).isEqualTo(collectionName);

            List<Collection> collections = client.collection().list(
                CollectionListRequest.create()
                    .search(collectionName)
                    .firstResult(0)
                    .maxResult(20)
            );

            assertThat(collections)
                .extracting(Collection::getId)
                .contains(collection.getId());

            int count = client.collection().count(
                CollectionListRequest.create()
                    .search(collectionName)
            );

            assertThat(count).isGreaterThanOrEqualTo(1);

            List<Collection> availableCollections = client.collection().listByPermissions();

            assertThat(availableCollections)
                .extracting(Collection::getId)
                .contains(collection.getId());

            Collection updated = client.collection().update(
                UpdateCollectionRequest.create()
                    .collectionId(collection.getId())
                    .name(collectionName + "-updated")
                    .description("Updated by AprilFlow Java SDK collection")
            );

            assertThat(updated).isNotNull();
            assertThat(updated.getId()).isEqualTo(collection.getId());
            assertThat(updated.getName()).isEqualTo(collectionName + "-updated");

            collection = updated;

            Collection publicCollection = client.collection().setVisibility(
                SetCollectionVisibilityRequest.create()
                    .collectionId(collection.getId())
                    .visibility(CollectionVisibility.Public)
            );

            assertThat(publicCollection).isNotNull();
            assertThat(publicCollection.getId()).isEqualTo(collection.getId());

            List<String> policyIds = client.collection().listPolicyIds(collection.getId());

            assertThat(policyIds).isNotNull();

            client.collection().delete(collection.getId());
            deleted = true;

            int deletedCount = client.collection().countDeleted();

            assertThat(deletedCount).isGreaterThanOrEqualTo(1);

            List<Collection> deletedCollections = client.collection().listDeleted(0, 50);

            assertThat(deletedCollections)
                .extracting(Collection::getId)
                .contains(collection.getId());

            List<Collection> restored = client.collection().restore(
                List.of(collection.getId())
            );

            assertThat(restored)
                .extracting(Collection::getId)
                .contains(collection.getId());

            deleted = false;

            client.collection().delete(collection.getId());
            deleted = true;
        }
        finally
        {
            if (collection != null && collection.getId() != null && !deleted)
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

    @Test
    public void testUserPromptCollection()
    {
        AprilFlowClient client = initTest();

        Collection existing = client.collection().getUserPromptCollection();

        if (existing != null)
        {
            assertThat(existing.getId()).isNotBlank();
            assertThat(existing.getVisibility()).isEqualTo(CollectionVisibility.User);

            client.collection().delete(existing.getId());

            return;
        }

        Collection created = client.collection().createUserPromptCollection();

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotBlank();
        assertThat(created.getVisibility()).isEqualTo(CollectionVisibility.User);

        created = client.collection().getUserPromptCollection();

        assertThat(created.getId()).isNotBlank();
        assertThat(created.getVisibility()).isEqualTo(CollectionVisibility.User);

        client.collection().delete(created.getId());
    }
}
