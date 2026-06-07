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

from aprilflow import Policy, PolicyListRequest, TenantRole

pytestmark = pytest.mark.integration


def test_policy_lifecycle(client, unique_name):
    tenant = client.identity.tenant.get()
    policy = None
    try:
        policy = client.identity.policy.insert(
            Policy(
                name=unique_name,
                description="Policy created from Python SDK integration tests",
                tenant_id=tenant.id,
                roles=[TenantRole.COLLECTION_READ],
            )
        )
        assert policy.id
        assert client.identity.policy.count(PolicyListRequest(search=unique_name)) >= 1
        policies = client.identity.policy.list(search=unique_name, first_result=0, max_result=10)
        assert any(item.id == policy.id for item in policies)

        policy.description = "Updated from Python SDK integration tests"
        updated = client.identity.policy.update(policy)
        assert updated.description == policy.description
    finally:
        if policy and policy.id:
            try:
                client.identity.policy.delete(policy.id)
            except Exception:
                pass
