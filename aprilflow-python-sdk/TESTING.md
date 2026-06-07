# AprilFlow Python SDK Tests

The Python SDK contains two levels of tests, matching the intent of the Java SDK tests:

1. Fast offline tests under `tests/` for models, authentication, HTTP behavior, resource wiring, SSE parsing, and wait-helper logic.
2. Live integration tests under `tests/integration/` that call a real AprilFlow backend and exercise the same API groups as the Java SDK integration tests.

Use a dedicated test tenant or development environment. Do not run live tests against production unless that is intentional.

## Requirements

- Python 3.10+
- A reachable AprilFlow backend for integration tests
- A user key with permissions for the tests being executed

Install the SDK with test dependencies:

```bash
python -m pip install -e ".[test]"
```

## Configuration for live tests

Live tests read configuration from environment variables:

```bash
export APRILFLOW_BASE_URL=https://api.aprilflow.ai
export APRILFLOW_USER_KEY=your-user-key
```

Do not commit real user keys. Use local environment variables, secret injection, or CI-provided secrets.

If either variable is missing or blank, the integration tests are skipped with `pytest.skip`.

## Running tests

Run all tests. Integration tests are skipped automatically when credentials are not present:

```bash
pytest
```

Run only offline tests:

```bash
pytest tests --ignore=tests/integration
```

Run only live integration tests:

```bash
pytest -m integration
```

Run a single integration file:

```bash
pytest tests/integration/test_upload_live.py -m integration
```

Run with coverage:

```bash
pytest --cov=aprilflow --cov-report=term-missing
```

## Offline test coverage

| Test file | Coverage |
|---|---|
| `tests/test_models.py` | Pydantic model parsing, aliases, enum values, and serialization. |
| `tests/test_auth.py` | Identity token parsing, token caching, refresh behavior, and invalidation. |
| `tests/test_http.py` | Authorization headers, query cleanup, URL path encoding, HTTP error handling, and streaming response checks. |
| `tests/test_resources.py` | Endpoint paths and payloads for collection, upload, document, prompt/session, identity, billing, and message resources. |
| `tests/test_notifications.py` | SSE event parsing, notification listener dispatch, subscription lifecycle, and upload/prompt wait-helper behavior. |

## Live integration test coverage

The live test files intentionally mirror the Java SDK test classes.

| Python test file | Java equivalent | Coverage |
|---|---|---|
| `tests/integration/test_collection_live.py` | `CollectionTest` | Collection create, list, count, permissions list, update, visibility change, policy ID list, delete, deleted list, restore, and user prompt collection. |
| `tests/integration/test_country_live.py` | `CountryTest` | Country listing. |
| `tests/integration/test_document_live.py` | `DocumentTest` | Document search against an empty temporary collection. |
| `tests/integration/test_message_live.py` | `MessageTest` | Support thread creation. |
| `tests/integration/test_policy_live.py` | `PolicyTest` | Tenant read, policy creation, count, list, update, and deletion. |
| `tests/integration/test_prompt_live.py` | `PromptTest` | Prompt session creation with `create_and_wait(...)` and notification-driven completion. |
| `tests/integration/test_quota_live.py` | `QuotaTest` | Quota type list, usage list, quota create, count, list, update, and delete. |
| `tests/integration/test_upload_live.py` | `UploadTest` | Byte, file, and stream uploads with notification-driven processing wait, plus upload list/count. |
| `tests/integration/test_user_key_live.py` | `UserKeyTest` | User key create, list, revoke, and delete. |
| `tests/integration/test_user_live.py` | `UserTest` | Tenant read, policy setup, user invite, count, list, get, enable/disable, TOTP toggles, policy update, email change call, policy user listing/removal, and delete. |

## Notes for CI

A typical CI setup should run offline tests for every pull request. Live tests should run in a controlled environment where `APRILFLOW_BASE_URL` and `APRILFLOW_USER_KEY` are injected securely.

Example:

```bash
pytest tests --ignore=tests/integration
pytest -m integration
```

The integration tests create and delete resources. They use unique names and `try/finally` cleanup blocks, but they still require a tenant where test data creation is allowed.
