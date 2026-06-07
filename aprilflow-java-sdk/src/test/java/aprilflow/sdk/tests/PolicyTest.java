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
import aprilflow.sdk.identity.Policy;
import aprilflow.sdk.identity.PolicyListRequest;
import aprilflow.sdk.identity.Tenant;
import aprilflow.sdk.identity.TenantRole;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class PolicyTest extends BaseTest
{
    @Test
    void testPolicy()
    {
        AprilFlowClient client = initTest();

        Policy policy = null;

        try
        {
            Tenant tenant = client.identity().tenant().get();

            assertThat(tenant).isNotNull();
            assertThat(tenant.getId()).isNotBlank();
            
            String policyName = getTestName("java-sdk-it-policy");

            policy = Policy.create()
                .tenantId(tenant.getId())
                .name(policyName)
                .description("Temporary policy created by AprilFlow Java SDK tests")
                .roles(List.of(
                    TenantRole.CollectionRead,
                    TenantRole.PromptCreate,
                    TenantRole.PromptRead
                ))
                .build();

            Policy created = client.identity().policy().insert(policy);

            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getName()).isEqualTo(policyName);

            policy = created;

            int count = client.identity().policy().count(
                PolicyListRequest.create()
                    .search(policyName)
            );

            assertThat(count).isGreaterThanOrEqualTo(1);

            List<Policy> policies = client.identity().policy().list(
                PolicyListRequest.create()
                    .search(policyName)
                    .firstResult(0)
                    .maxResult(20)
            );

            assertThat(policies)
                .extracting(Policy::getId)
                .contains(policy.getId());

            Policy update = Policy.create()
                .id(policy.getId())
                .name(policyName + "-updated")
                .description("Updated temporary policy created by AprilFlow Java SDK tests")
                .roles(List.of(
                    TenantRole.CollectionRead,
                    TenantRole.CollectionUpdate,
                    TenantRole.PromptCreate,
                    TenantRole.PromptRead
                ))
                .build();

            Policy updated = client.identity().policy().update(update);

            assertThat(updated).isNotNull();
            assertThat(updated.getId()).isEqualTo(policy.getId());
            assertThat(updated.getName()).isEqualTo(policyName + "-updated");

            policy = updated;

            client.identity().policy().delete(policy.getId());

            policy = null;
        }
        finally
        {
            if (client != null && policy != null && policy.getId() != null)
            {
                try
                {
                    client.identity().policy().delete(policy.getId());
                }
                catch (RuntimeException ignored)
                {
                }
            }
        }
    }
}
