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
import aprilflow.sdk.billing.Quota;
import aprilflow.sdk.billing.QuotaListRequest;
import aprilflow.sdk.billing.QuotaScope;
import aprilflow.sdk.billing.QuotaTarget;
import aprilflow.sdk.billing.QuotaType;
import aprilflow.sdk.billing.Usage;
import aprilflow.sdk.identity.Tenant;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class QuotaTest extends BaseTest
{
    @Test
    void testQuota()
    {
        AprilFlowClient client = initTest();

        Quota quota = null;

        try
        {
            Tenant tenant = client.identity().tenant().get();

            assertThat(tenant).isNotNull();
            assertThat(tenant.getId()).isNotBlank();

            List<QuotaType> quotaTypes = client.billing().quotaType().list();

            assertThat(quotaTypes).isNotNull();
            assertThat(quotaTypes).isNotEmpty();

            QuotaType quotaType = quotaTypes.get(0);

            assertThat(quotaType.getId()).isNotBlank();

            List<Usage> usages = client.billing().usage().list();

            assertThat(usages).isNotNull();

            Usage usage = usages.get(0);

            assertThat(usage.getId()).isNotBlank();

            int beforeCount = client.billing().quota().count();

            quota = Quota.create()
                .tenantId(tenant.getId())
                .quotaScope(QuotaScope.Tenant)
                .quotaTarget(QuotaTarget.Tenant)
                .quotaWindow(30)
                .usageId(usage.getId())
                .quotaTypeId(quotaType.getId())
                .value(1000)
                .description(getTestName("java-sdk-it-quota"))
                .build();

            Quota created = client.billing().quota().insert(quota);

            assertThat(created).isNotNull();
            assertThat(created.getId()).isNotBlank();
            assertThat(created.getQuotaTypeId()).isEqualTo(quotaType.getId());
            assertThat(created.getValue()).isEqualTo(1000);

            quota = created;

            int afterCreateCount = client.billing().quota().count();

            assertThat(afterCreateCount).isGreaterThanOrEqualTo(beforeCount + 1);

            List<Quota> quotas = client.billing().quota().list(
                QuotaListRequest.create()
                    .firstResult(0)
                    .maxResult(Math.max(afterCreateCount + 10, 20))
            );

            assertThat(quotas)
                .extracting(Quota::getId)
                .contains(quota.getId());

            Quota update = Quota.create()
                .id(quota.getId())
                .tenantId(tenant.getId())
                .quotaScope(QuotaScope.Tenant)
                .quotaTarget(QuotaTarget.User)
                .usageId(usage.getId())
                .quotaTypeId(quotaType.getId())
                .quotaWindow(30)
                .value(2000)
                .description(getTestName("java-sdk-it-quota-updated"))
                .build();

            Quota updated = client.billing().quota().update(update);

            assertThat(updated).isNotNull();
            assertThat(updated.getId()).isEqualTo(quota.getId());
            assertThat(updated.getValue()).isEqualTo(2000);

            quota = updated;

            client.billing().quota().delete(quota.getId());

            quota = null;

            int afterDeleteCount = client.billing().quota().count();

            assertThat(afterDeleteCount).isLessThanOrEqualTo(afterCreateCount);
        }
        finally
        {
            if (client != null && quota != null && quota.getId() != null)
            {
                try
                {
                    client.billing().quota().delete(quota.getId());
                }
                catch (RuntimeException ignored)
                {
                }
            }
        }
    }
}
