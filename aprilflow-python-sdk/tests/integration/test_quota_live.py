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

import pytest

from aprilflow import Quota, QuotaListRequest, QuotaScope, QuotaTarget

pytestmark = pytest.mark.integration


def test_quota_lifecycle(client, unique_name):
    tenant = client.identity.tenant.get()
    quota_types = client.billing.quota_type.list()
    usages = client.billing.usage.list()
    assert isinstance(quota_types, list)
    assert isinstance(usages, list)
    if not quota_types or not usages:
        pytest.skip("Quota types and usages are required for quota lifecycle integration test")

    quota = None
    before_count = client.billing.quota.count()
    try:
        quota = client.billing.quota.insert(
            Quota(
                tenant_id=tenant.id,
                quota_target=QuotaTarget.TENANT,
                quota_scope=QuotaScope.TENANT,
                usage_id=usages[0].id,
                quota_type_id=quota_types[0].id,
                quota_window=1,
                description=unique_name,
                value=1000,
            )
        )
        assert quota.id
        assert client.billing.quota.count() >= before_count
        quotas = client.billing.quota.list(QuotaListRequest(first_result=0, max_result=50))
        assert isinstance(quotas, list)
        quota.description = f"{unique_name}-updated"
        quota.value = 2000
        updated = client.billing.quota.update(quota)
        assert updated.value == 2000
    finally:
        if quota and quota.id:
            try:
                client.billing.quota.delete(quota.id)
            except Exception:
                pass
