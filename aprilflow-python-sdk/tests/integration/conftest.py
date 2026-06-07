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

import os
import uuid
from pathlib import Path

import pytest

from aprilflow import AprilFlowClient


def pytest_configure(config):
    config.addinivalue_line("markers", "integration: tests that require a live AprilFlow API and credentials")


@pytest.fixture(scope="session")
def live_config():
    base_url = os.getenv("APRILFLOW_BASE_URL", "").strip()
    user_key = os.getenv("APRILFLOW_USER_KEY", "").strip()
    if not base_url or not user_key:
        pytest.skip("APRILFLOW_BASE_URL and APRILFLOW_USER_KEY are required for live integration tests")
    return base_url, user_key


@pytest.fixture(scope="session")
def client(live_config):
    base_url, user_key = live_config
    sdk = AprilFlowClient.create(base_url=base_url, user_key=user_key)
    try:
        yield sdk
    finally:
        sdk.close()


@pytest.fixture
def unique_name():
    return f"python-sdk-test-{uuid.uuid4().hex[:10]}"


@pytest.fixture
def sample_file(tmp_path: Path):
    path = tmp_path / "aprilflow-python-sdk-upload.txt"
    path.write_text("This is a Python SDK integration test upload.\n", encoding="utf-8")
    return path
