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
import aprilflow.sdk.identity.Tenant;
import aprilflow.sdk.identity.TenantRole;
import aprilflow.sdk.identity.User;
import aprilflow.sdk.identity.UserListRequest;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class UserTest extends BaseTest
{
    @Test
    void testUser()
    {
        AprilFlowClient client = initTest();

        Policy policy = null;
        User user = null;

        try
        {
            Tenant tenant = client.identity().tenant().get();

            assertThat(tenant).isNotNull();
            assertThat(tenant.getId()).isNotBlank();

            String policyName = getTestName("java-sdk-it-user-policy");

            policy = client.identity().policy().insert(
                Policy.create()
                    .tenantId(tenant.getId())
                    .name(policyName)
                    .description("Temporary policy created by AprilFlow Java SDK user test")
                    .roles(List.of(
                        TenantRole.CollectionRead,
                        TenantRole.PromptCreate,
                        TenantRole.PromptRead
                    ))
                    .build()
            );

            assertThat(policy).isNotNull();
            assertThat(policy.getId()).isNotBlank();

            String email = "sdk-test1@aprilflow.ai";

            user = client.identity().user().invite(
                email,
                List.of(policy.getId())
            );

            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotBlank();
            assertThat(user.getEmail()).isEqualToIgnoringCase(email);

            int count = client.identity().user().count(
                UserListRequest.create()
                    .search(email)
            );

            assertThat(count).isGreaterThanOrEqualTo(1);

            List<User> users = client.identity().user().list(
                UserListRequest.create()
                    .search(email)
                    .firstResult(0)
                    .maxResult(20)
            );

            assertThat(users)
                .extracting(User::getId)
                .contains(user.getId());

            User fetched = client.identity().user().get(user.getId());

            assertThat(fetched).isNotNull();
            assertThat(fetched.getId()).isEqualTo(user.getId());
            assertThat(fetched.getEmail()).isEqualToIgnoringCase(email);

            User disabled = client.identity().user().disable(user.getId());

            assertThat(disabled).isNotNull();
            assertThat(disabled.getId()).isEqualTo(user.getId());

            User enabled = client.identity().user().enable(user.getId());

            assertThat(enabled).isNotNull();
            assertThat(enabled.getId()).isEqualTo(user.getId());

            User totpDisabled = client.identity().user().disableTotp(user.getId());

            assertThat(totpDisabled).isNotNull();
            assertThat(totpDisabled.getId()).isEqualTo(user.getId());

            User totpEnabled = client.identity().user().enableTotp(user.getId());

            assertThat(totpEnabled).isNotNull();
            assertThat(totpEnabled.getId()).isEqualTo(user.getId());

            User policiesUpdated = client.identity().user().updatePolicies(
                user.getId(),
                List.of(policy.getId())
            );

            assertThat(policiesUpdated).isNotNull();
            assertThat(policiesUpdated.getId()).isEqualTo(user.getId());

            String changedEmail = "sdk-test2@aprilflow.ai";

            client.identity().user().changeEmail(
                user.getId(),
                changedEmail
            );

            User afterEmailChange = client.identity().user().get(user.getId());

            assertThat(afterEmailChange).isNotNull();
            assertThat(afterEmailChange.getEmail()).isEqualToIgnoringCase(email);

            List<User> policyUsers = client.identity().policy().listUsers(policy.getId());

            assertThat(policyUsers).isNotNull();

            client.identity().policy().removeUsers(
                policy.getId(),
                List.of(user.getId())
            );

            client.identity().user().delete(user.getId());

            user = null;
        }
        finally
        {
            if (client != null)
            {
                if (user != null && user.getId() != null)
                {
                    try
                    {
                        client.identity().user().delete(user.getId());
                    }
                    catch (RuntimeException ignored)
                    {
                    }
                }

                if (policy != null && policy.getId() != null)
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
}
