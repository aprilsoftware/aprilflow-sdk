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
from pydantic import TypeAdapter
from ..http import APIClient, segment
from ..models.identity import *

class TenantResource:
    def __init__(self, http: APIClient): self._http = http
    def get(self) -> Tenant:
        return Tenant.model_validate(self._http.request("GET", "/identity/v1/pub/tenants").json())

class CountryResource:
    def __init__(self, http: APIClient): self._http = http
    def list(self) -> list[Country]:
        return TypeAdapter(list[Country]).validate_python(self._http.request("GET", "/identity/v1/pub/countries").json())

class PolicyResource:
    def __init__(self, http: APIClient): self._http = http
    def count(self, request: PolicyListRequest | None = None, **kwargs) -> int:
        req = request or PolicyListRequest(**kwargs)
        return int(self._http.request("GET", "/identity/v1/pub/tenants/policies/count", params=req.to_api_json()).text)
    def list(self, request: PolicyListRequest | None = None, **kwargs) -> list[Policy]:
        req = request or PolicyListRequest(**kwargs)
        return TypeAdapter(list[Policy]).validate_python(self._http.request("GET", "/identity/v1/pub/tenants/policies", params=req.to_api_json()).json())
    def insert(self, policy: Policy) -> Policy:
        return Policy.model_validate(self._http.request("POST", "/identity/v1/pub/tenants/policies", json=policy.to_api_json()).json())
    def update(self, policy: Policy) -> Policy:
        return Policy.model_validate(self._http.request("PUT", "/identity/v1/pub/tenants/policies", json=policy.to_api_json()).json())
    def delete(self, policy_id: str) -> None:
        self._http.request("DELETE", f"/identity/v1/pub/tenants/policies/{segment(policy_id)}")
    def list_users(self, policy_id: str) -> list[User]:
        return TypeAdapter(list[User]).validate_python(self._http.request("GET", f"/identity/v1/pub/tenants/policies/{segment(policy_id)}/users").json())
    def remove_users(self, policy_id: str, user_ids: list[str]) -> None:
        self._http.request("DELETE", f"/identity/v1/pub/tenants/policies/{segment(policy_id)}/users", json=user_ids)

class UserResource:
    def __init__(self, http: APIClient): self._http = http
    def get(self, user_id: str) -> User: return User.model_validate(self._http.request("GET", f"/identity/v1/pub/tenants/users/{segment(user_id)}").json())
    def count(self, request: UserListRequest | None = None, **kwargs) -> int:
        req = request or UserListRequest(**kwargs); return int(self._http.request("GET", "/identity/v1/pub/tenants/users/count", params=req.to_api_json()).text)
    def list(self, request: UserListRequest | None = None, **kwargs) -> list[User]:
        req = request or UserListRequest(**kwargs); return TypeAdapter(list[User]).validate_python(self._http.request("GET", "/identity/v1/pub/tenants/users", params=req.to_api_json()).json())
    def invite(self, email: str, policy_ids: list[str]) -> User:
        return User.model_validate(self._http.request("POST", "/identity/v1/pub/tenants/users/invite", params={"email": email}, json=policy_ids).json())
    def update_info(self, user: User) -> None: self._http.request("PUT", f"/identity/v1/pub/tenants/users/{segment(user.id)}", json=user.to_api_json())
    def delete(self, user_id: str) -> None: self._http.request("DELETE", f"/identity/v1/pub/tenants/users/{segment(user_id)}")
    def update_policies(self, user_id: str, policy_ids: list[str]) -> User: return User.model_validate(self._http.request("PUT", f"/identity/v1/pub/tenants/users/{segment(user_id)}/policies", json=policy_ids).json())
    def set_enabled(self, user_id: str, enabled: bool) -> User: return User.model_validate(self._http.request("PUT", f"/identity/v1/pub/tenants/users/{segment(user_id)}/{'enable' if enabled else 'disable'}").json())
    def enable(self, user_id: str) -> User: return self.set_enabled(user_id, True)
    def disable(self, user_id: str) -> User: return self.set_enabled(user_id, False)
    def set_totp_enabled(self, user_id: str, enabled: bool) -> User: return User.model_validate(self._http.request("PUT", f"/identity/v1/pub/tenants/users/{segment(user_id)}/totp/{'enable' if enabled else 'disable'}").json())
    def enable_totp(self, user_id: str) -> User: return self.set_totp_enabled(user_id, True)
    def disable_totp(self, user_id: str) -> User: return self.set_totp_enabled(user_id, False)
    def change_email(self, user_id: str, email: str) -> None: self._http.request("PUT", f"/identity/v1/pub/tenants/users/{segment(user_id)}/email", params={"id": user_id, "email": email})

class UserKeyResource:
    def __init__(self, http: APIClient): self._http = http
    def count(self) -> int: return int(self._http.request("GET", "/identity/v1/pub/tenants/users/keys/count").text)
    def list(self, request: UserKeyListRequest | None = None, **kwargs) -> list[UserKey]:
        req = request or UserKeyListRequest(**kwargs); return TypeAdapter(list[UserKey]).validate_python(self._http.request("GET", "/identity/v1/pub/tenants/users/keys", params=req.to_api_json()).json())
    def create(self) -> str: return self._http.request("POST", "/identity/v1/pub/tenants/users/keys").text
    def revoke(self, key_id: str) -> UserKey: return UserKey.model_validate(self._http.request("PUT", f"/identity/v1/pub/tenants/users/keys/{segment(key_id)}").json())
    def delete(self, key_id: str | UserKey) -> None:
        if isinstance(key_id, UserKey): key_id = key_id.key_id
        self._http.request("DELETE", f"/identity/v1/pub/tenants/users/keys/{segment(key_id)}")

class IdentityResource:
    def __init__(self, http: APIClient):
        self.tenant = TenantResource(http); self.country = CountryResource(http); self.policy = PolicyResource(http); self.user = UserResource(http); self.user_key = UserKeyResource(http)
