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

from aprilflow import UserKeyListRequest

pytestmark = pytest.mark.integration


def test_user_key_lifecycle(client):
    created_raw_user_key = None
    created_user_key = None
    before_count = client.identity.user_key.count()
    try:
        created_raw_user_key = client.identity.user_key.create()
        assert created_raw_user_key
        keys = client.identity.user_key.list(UserKeyListRequest(first_result=0, max_result=50))
        created_user_key = next((key for key in keys if key.key_id and created_raw_user_key.endswith(key.key_id)), None)
        if created_user_key is None:
            created_user_key = keys[0]
        assert client.identity.user_key.count() >= before_count
        revoked = client.identity.user_key.revoke(created_user_key.key_id)
        assert revoked.key_id == created_user_key.key_id
    finally:
        if created_user_key and created_user_key.key_id:
            try:
                client.identity.user_key.delete(created_user_key.key_id)
            except Exception:
                pass
