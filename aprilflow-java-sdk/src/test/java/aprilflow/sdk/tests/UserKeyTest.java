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
import aprilflow.sdk.identity.UserKey;
import aprilflow.sdk.identity.UserKeyListRequest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class UserKeyTest extends BaseTest
{
    @Test
    void testUserKey()
    {
        AprilFlowClient client = initTest();

        String createdRawUserKey = null;
        UserKey createdUserKey = null;

        try
        {
            int beforeCount = client.identity().userKey().count();

            createdRawUserKey = client.identity().userKey().create();

            assertThat(createdRawUserKey).isNotNull();
            assertThat(createdRawUserKey).isNotBlank();

            List<UserKey> userKeysAfterCreate = client.identity().userKey().list(
                UserKeyListRequest.create()
                    .firstResult(0)
                    .maxResult(Math.max(beforeCount + 10, 20))
            );

            assertThat(userKeysAfterCreate).isNotNull();

            createdUserKey = userKeysAfterCreate.get(0);

            assertThat(createdUserKey).isNotNull();
            assertThat(createdUserKey.getKeyId()).isNotBlank();

            int afterCreateCount = client.identity().userKey().count();

            assertThat(afterCreateCount).isGreaterThanOrEqualTo(beforeCount + 1);

            UserKey revokedApiKey = client.identity().userKey().revoke(createdUserKey.getKeyId());

            assertThat(revokedApiKey).isNotNull();
            assertThat(revokedApiKey.getKeyId()).isEqualTo(createdUserKey.getKeyId());
            assertThat(revokedApiKey.getRevokedDate() != null).isTrue();

            client.identity().userKey().delete(createdUserKey.getKeyId());

            createdUserKey = null;

            int afterDeleteCount = client.identity().userKey().count();

            assertThat(afterDeleteCount).isLessThanOrEqualTo(afterCreateCount);
        }
        finally
        {
            if (client != null && createdUserKey != null && createdUserKey.getKeyId() != null)
            {
                try
                {
                    client.identity().userKey().delete(createdUserKey.getKeyId());
                }
                catch (RuntimeException ignored)
                {
                }
            }
        }
    }
}
