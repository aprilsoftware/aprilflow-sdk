# AprilFlow Java SDK Tests

The project contains JUnit 5 integration tests under:

```text
src/test/java/aprilflow/sdk/tests
```

The tests call a real AprilFlow backend. They create, update, process, and delete real resources in the configured environment.

Use a dedicated test tenant or development environment. Do not run these tests against production unless that is intentional.

## Requirements

- Java 17+
- Maven
- A reachable AprilFlow backend
- A user key with permissions for the tests being executed

## Configuration

## Configuration

Tests read configuration from environment variables.

Required variables:

```bash
export APRILFLOW_BASE_URL="https://api.aprilflow.ai"
export APRILFLOW_USER_KEY="your-user-key"

Do not commit real user keys. Use a local file, secret injection, or CI-generated configuration.

`BaseTest` loads the properties file and creates a client with:

```java
AprilFlowClient.builder()
    .baseUrl(baseUrl)
    .userKey(userKey)
    .build();
```

If the base URL or user key is missing or blank, tests are skipped with JUnit assumptions.

## Running tests

Run all tests:

```bash
mvn test
```

Run a single test class:

```bash
mvn -Dtest=CollectionTest test
```

Run a single test method:

```bash
mvn -Dtest=UploadTest#testUpload1 test
```

## Test classes

| Test class | Coverage |
|---|---|
| `CollectionTest` | Collection create, list, count, permissions list, update, visibility change, policy ID list, delete, deleted list, restore, and user prompt collection. |
| `CountryTest` | Country listing. |
| `DocumentTest` | Document search against an empty temporary collection. |
| `MessageTest` | Support thread creation. |
| `PolicyTest` | Tenant read, policy creation, count, list, update, and deletion. |
| `PromptTest` | Prompt session creation with `createAndWait(...)` and notification-driven prompt completion. |
| `QuotaTest` | Quota type list, usage list, quota create, count, list, update, and delete. |
| `UploadTest` | Byte, file, and stream uploads with notification-driven processing wait. |
| `UserKeyTest` | User key create, list, revoke, and delete. |
| `UserTest` | Tenant read, policy setup, user invite, count, list, get, enable/disable, TOTP toggles, policy update, email change call, policy user listing/removal, and delete. |

