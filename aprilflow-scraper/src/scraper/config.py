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

from dataclasses import dataclass
import os


@dataclass(frozen=True)
class AprilFlowConfig:
    base_url: str
    user_key: str


def load_aprilflow_config() -> AprilFlowConfig:
    base_url = os.environ.get("APRILFLOW_BASE_URL")
    user_key = os.environ.get("APRILFLOW_USER_KEY")

    missing = [
        name
        for name, value in [
            ("APRILFLOW_BASE_URL", base_url),
            ("APRILFLOW_USER_KEY", user_key),
        ]
        if not value
    ]

    if missing:
        raise SystemExit(
            "Missing configuration env vars: " + ", ".join(missing) + "\n"
            "Example:\n"
            "  export APRILFLOW_BASE_URL='https://api.aprilflow.ai'\n"
            "  export APRILFLOW_USER_KEY='...'\n"
        )

    return AprilFlowConfig(base_url=base_url.rstrip("/"), user_key=user_key)
