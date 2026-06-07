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

from aprilflow import Policy, TenantRole

pytestmark = pytest.mark.integration


def test_user_lifecycle(client, unique_name):
    tenant = client.identity.tenant.get()
    policy = None
    user = None
    email = f"{unique_name}@example.com"
    try:
        policy = client.identity.policy.insert(
            Policy(
                name=f"{unique_name}-policy",
                description="Policy for Python SDK user integration test",
                tenant_id=tenant.id,
                roles=[TenantRole.USER_READ],
            )
        )
        user = client.identity.user.invite(email=email, policy_ids=[policy.id])
        assert user.id
        assert client.identity.user.count(search=email) >= 1
        users = client.identity.user.list(search=email, first_result=0, max_result=10)
        assert any(item.id == user.id for item in users)
        fetched = client.identity.user.get(user.id)
        assert fetched.id == user.id
        assert client.identity.user.disable(user.id).enabled is False
        assert client.identity.user.enable(user.id).enabled is True
        assert client.identity.user.disable_totp(user.id).totp_enabled is False
        client.identity.user.enable_totp(user.id)
        policies_updated = client.identity.user.update_policies(user.id, [policy.id])
        assert policy.id in policies_updated.policy_ids
        client.identity.user.change_email(user.id, f"updated-{email}")
        policy_users = client.identity.policy.list_users(policy.id)
        assert isinstance(policy_users, list)
        client.identity.policy.remove_users(policy.id, [user.id])
    finally:
        if user and user.id:
            try:
                client.identity.user.delete(user.id)
            except Exception:
                pass
        if policy and policy.id:
            try:
                client.identity.policy.delete(policy.id)
            except Exception:
                pass
