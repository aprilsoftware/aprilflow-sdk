# AprilFlow Python SDK

Python SDK for calling AprilFlow APIs from server-side or programmatic Python applications.

The SDK provides typed Python APIs for collections, uploads, documents, prompts, identity, billing, support messages, and notification streams. It uses `httpx` for HTTP and `pydantic` for typed request and response models.

## Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Creating a client](#creating-a-client)
- [Authentication](#authentication)
- [Error handling](#error-handling)
- [Collections](#collections)
- [Uploads](#uploads)
- [Documents](#documents)
- [Prompts and sessions](#prompts-and-sessions)
- [Notifications](#notifications)
- [Identity](#identity)
- [Billing](#billing)
- [Messages](#messages)
- [Custom clients and serialization](#custom-clients-and-serialization)
- [API summary](#api-summary)

## Requirements

- Python 3.10+
- An AprilFlow base URL
- An AprilFlow user key

Runtime dependencies:

- `httpx`
- `pydantic`

## Installation

For local development, install the package in editable mode:

```bash
pip install -e .
```

Or install with notification dependencies:

```bash
pip install -e '.[notifications]'
```

For test dependencies:

```bash
pip install -e '.[test]'
```

## Creating a client

The simplest way to create a client is with `AprilFlowClient.create(...)`:

```python
import os
from aprilflow import AprilFlowClient

client = AprilFlowClient.create(
    base_url="https://api.aprilflow.ai",
    user_key=os.environ["APRILFLOW_USER_KEY"],
)

print(client.prompt.built_with())
```

You can also instantiate the client directly:

```python
from aprilflow import AprilFlowClient

client = AprilFlowClient(
    base_url="https://api.aprilflow.ai",
    user_key="YOUR_USER_KEY",
)
```

The top-level client exposes the following API groups:

```python
client.collection
client.upload
client.document
client.prompt
client.identity
client.billing
client.message
client.notification
```

Close the underlying HTTP client when you are done:

```python
client.close()
```

## Authentication

The SDK authenticates with an AprilFlow user key. The client creates a `UserKeyTokenProvider`, obtains an access token, and then uses bearer authentication for API requests.

Keep user keys on trusted server-side systems only. Do not embed them in browser or mobile applications.

## Error handling

The SDK raises runtime exceptions.

Typical exceptions include:

- `ValueError` or Pydantic validation errors for invalid local request data.
- `AprilFlowError` for SDK-level errors.
- `AprilFlowHTTPError` for non-2xx API responses.

Example:

```python
from aprilflow import AprilFlowClient, AprilFlowHTTPError

client = AprilFlowClient.create(
    base_url="https://api.aprilflow.ai",
    user_key="YOUR_USER_KEY",
)

try:
    version = client.prompt.built_with()
    print(version)
except AprilFlowHTTPError as exc:
    print(exc.status_code)
    print(exc.response_body)
```

## Collections

Collections group uploaded content and can be used by prompts.

### Create a collection

You can use keyword arguments:

```python
from aprilflow import CollectionVisibility

collection = client.collection.create(
    name="SDK example collection",
    description="Collection created from the Python SDK",
    visibility=CollectionVisibility.PRIVATE,
)

print(collection.id)
```

Or create a typed request model explicitly:

```python
from aprilflow import CreateCollectionRequest, CollectionVisibility

collection = client.collection.create(
    CreateCollectionRequest(
        name="SDK example collection",
        description="Collection created from the Python SDK",
        visibility=CollectionVisibility.PRIVATE,
    )
)
```

### List and count collections

```python
from aprilflow import CollectionListRequest

count = client.collection.count(search="SDK")

collections = client.collection.list(
    CollectionListRequest(
        search="SDK",
        first_result=0,
        max_result=20,
    )
)
```

### Update a collection

```python
updated = client.collection.update(
    collection_id=collection.id,
    name="Updated collection name",
    description="Updated from the Python SDK",
)
```

### Set collection visibility

```python
from aprilflow import CollectionVisibility

updated = client.collection.set_visibility(
    collection_id=collection.id,
    visibility=CollectionVisibility.PUBLIC,
)
```

For restricted collections, pass policy IDs:

```python
updated = client.collection.set_visibility(
    collection_id=collection.id,
    visibility=CollectionVisibility.RESTRICTED,
    policy_ids=[policy_id],
)
```

### Deleted collections

```python
client.collection.delete(collection.id)

deleted_count = client.collection.count_deleted()
deleted_collections = client.collection.list_deleted(first_result=0, max_result=20)
restored = client.collection.restore([collection.id])
```

### User prompt collection

```python
user_prompt_collection = client.collection.get_user_prompt_collection()

if user_prompt_collection is None:
    user_prompt_collection = client.collection.create_user_prompt_collection()
```

### Other collection operations

```python
collections = client.collection.list_by_permissions()
policy_ids = client.collection.list_policy_ids(collection_id)
client.collection.delete(collection_id)
```

## Uploads

The upload API supports uploads from:

- bytes
- file paths
- file-like streams

The upload API also includes the Java SDK notification-driven wait helpers: `upload_file_and_wait`, `upload_bytes_and_wait`, `upload_stream_and_wait`, `watch`, and `wait_for_upload`.


### Upload and wait for processing

```python
from aprilflow import UploadWaitOptions, UploadStatus

upload = client.upload.upload_file_and_wait(
    collection_id=collection.id,
    file="/path/to/document.pdf",
    wait_options=UploadWaitOptions(
        timeout=300,
        terminal_statuses=[
            UploadStatus.PROCESSED,
            UploadStatus.IGNORED,
            UploadStatus.CANCELED,
            UploadStatus.ON_ERROR,
            UploadStatus.DOCUMENT_DELETED,
            UploadStatus.QUOTA_EXCEEDED,
        ],
    ),
)

print(upload.status)
```

`upload_file_and_wait`, `upload_bytes_and_wait`, and `upload_stream_and_wait` open an SSE notification subscription after the upload is created, then wait until a terminal upload status is received.

### Watch a specific upload

```python
def on_upload_notification(notification):
    print(notification.action, notification.object_type, notification.object)

subscription = client.upload.watch(
    collection_id=collection.id,
    upload_id=upload.id,
    listener=on_upload_notification,
)

# Later, stop listening.
subscription.close()
```

### Upload bytes

```python
upload = client.upload.upload_bytes(
    collection_id=collection.id,
    data=b"Hello from the SDK",
    file_name="example.txt",
    content_type="text/plain",
)

print(upload.id)
```

### Upload a file

```python
from pathlib import Path

upload = client.upload.upload_file(
    collection_id=collection.id,
    file=Path("/path/to/document.pdf"),
    file_name="document.pdf",
    content_type="application/pdf",
)
```

### Upload a stream

```python
from io import BytesIO

stream = BytesIO(b"Stream content")

upload = client.upload.upload_stream(
    collection_id=collection.id,
    stream=stream,
    file_name="stream.txt",
    content_type="text/plain",
)
```

### List, count, and batch uploads

```python
from aprilflow import UploadListRequest, UploadStatus

uploads = client.upload.list(
    UploadListRequest(
        collection_id=collection.id,
        first_result=0,
        max_result=20,
    )
)

processed_uploads = client.upload.list(
    collection_id=collection.id,
    statuses=[UploadStatus.PROCESSED],
    first_result=0,
    max_result=20,
)

count = client.upload.count(collection_id=collection.id)

batch = client.upload.batch(
    collection_id=collection.id,
    upload_ids=[upload.id],
)
```

### Upload lifecycle operations

```python
canceled = client.upload.cancel(collection_id, upload_id)
restarted = client.upload.restart(collection_id, upload_id)

client.upload.delete_document(collection_id, upload_id)
client.upload.delete(collection_id, upload_id, delete_document=True)
```

## Documents

The document API provides document search and document object retrieval.

### Search documents in a collection

```python
from aprilflow import DocumentSearchRequest

documents = client.document.search(
    DocumentSearchRequest(
        collection_id=collection.id,
        text="search text",
        max_result=10,
    )
)
```

### Batch document items

```python
from aprilflow import DocumentId

items = client.document.batch_items([
    DocumentId(collection_id=collection_id, document_id=document_id)
])
```

### Retrieve document objects

```python
object_as_string = client.document.get_object_as_string(collection_id, document_id)
object_as_bytes = client.document.get_object_as_bytes(collection_id, document_id)
```

### Original document operations

```python
original = client.document.get_original_document(collection_id, document_id)

client.document.delete_original_document(collection_id, document_id)
```

### Delete a document

```python
client.document.delete(collection_id, document_id)
```

## Prompts and sessions

Prompt operations are accessed through:

```python
client.prompt
```

Session operations are accessed through:

```python
client.prompt.session
```

The prompt/session API includes the Java SDK notification-driven wait helpers: `create_and_wait`, `update_and_wait`, and `wait_for_completion`.


### Create a prompt session and wait

```python
from aprilflow import PromptRequest, PromptWaitOptions, PromptStatus

result = client.prompt.session.create_and_wait(
    PromptRequest(text="Please tell me a short story."),
    wait_options=PromptWaitOptions(
        timeout=300,
        terminal_statuses=[
            PromptStatus.COMPLETED,
            PromptStatus.INTERRUPTED,
            PromptStatus.ON_ERROR,
            PromptStatus.QUOTA_EXCEEDED,
        ],
    ),
)

print(result.prompt.status)
print(result.prompt.output)
```

`create_and_wait` and `update_and_wait` listen for `prompt.process` notifications for the prompt ID returned by the API call.

### Create a prompt session

```python
from aprilflow import PromptRequest

result = client.prompt.session.create(
    PromptRequest(text="Please tell me a short story.")
)

print(result.session.id)
print(result.prompt.id)
print(result.prompt.output)
```

### Create a prompt using collections or uploads

```python
from aprilflow import PromptRequest, PromptUploadId

result = client.prompt.session.create(
    PromptRequest(
        text="Summarize the uploaded content.",
        collection_ids=[collection.id],
        upload_ids=[
            PromptUploadId(collection_id=collection.id, upload_id=upload.id)
        ],
    )
)
```

### Update an existing session

```python
prompt = client.prompt.session.update(
    session_id,
    PromptRequest(text="Continue the answer."),
)
```

### Session management

```python
from aprilflow import SessionListRequest

updated = client.prompt.session.update_title(session_id, "New title")
duplicated = client.prompt.session.duplicate(session_id, "Copied session")

count = client.prompt.session.count()

sessions = client.prompt.session.list(
    SessionListRequest(first_result=0, max_result=20)
)

client.prompt.session.delete(session_id)
```

### Prompt operations

```python
prompts = client.prompt.list_by_session(session_id)

restarted = client.prompt.restart(prompt_id)
interrupted = client.prompt.interrupt(prompt_id)

client.prompt.delete(prompt_id)
```

### User prompt collections

```python
collections = client.prompt.list_user_collections()

client.prompt.set_user_collections([collection_id])
```

### Backend information

```python
built_with = client.prompt.built_with()
```

## Notifications

The notification API is represented by `client.notification` and implements `/message/v1/pub/notifications`. It sends `Accept: text/event-stream`, authenticates with the current bearer token, filters events named `notification`, deserializes JSON payloads into `Notification`, and invokes callbacks on a daemon background thread.

Most applications should prefer the higher-level wait helpers when they only need to wait for an upload or prompt to finish:

```python
client.upload.upload_file_and_wait(...)
client.prompt.session.create_and_wait(...)
```

Direct subscription is also available:

```python
from aprilflow import Notification

def on_notification(notification: Notification):
    print(notification.action, notification.object_type, notification.object)

def on_error(error: Exception):
    print("notification stream error", error)

subscription = client.notification.listen(
    ["collection.upload", "prompt"],
    on_notification,
    on_error,
)

# Later, stop the background SSE listener.
subscription.close()
```

The notification path helper is available:

```python
path = client.notification.path(["prompt"])
```

Expected upload processing notification metadata:

```text
action     = upload.process
objectType = collection.upload
```

Expected prompt processing notification metadata:

```text
action     = prompt.process
objectType = prompt
```

## Identity

Identity APIs are available through:

```python
client.identity
```

### Tenant and countries

```python
tenant = client.identity.tenant.get()
countries = client.identity.country.list()
```

### Policies

```python
from aprilflow import Policy, PolicyListRequest, TenantRole

policy = client.identity.policy.insert(
    Policy(
        name="SDK policy",
        description="Policy created from the Python SDK",
        tenant_id=tenant.id,
        roles=[TenantRole.PROMPT_CREATE, TenantRole.PROMPT_READ],
    )
)

count = client.identity.policy.count(
    PolicyListRequest(search="SDK")
)

policies = client.identity.policy.list(
    PolicyListRequest(search="SDK", first_result=0, max_result=20)
)

policy.name = "Updated SDK policy"
policy.description = "Updated policy"
policy.roles = [TenantRole.PROMPT_READ]
policy = client.identity.policy.update(policy)

client.identity.policy.delete(policy.id)
```

Policy users:

```python
users = client.identity.policy.list_users(policy_id)
client.identity.policy.remove_users(policy_id, [user_id])
```

### Users

```python
from aprilflow import UserListRequest

user = client.identity.user.invite(
    "new-user@example.com",
    [policy_id],
)

count = client.identity.user.count(
    UserListRequest(search="new-user@example.com")
)

users = client.identity.user.list(
    UserListRequest(search="new-user@example.com", first_result=0, max_result=20)
)

user = client.identity.user.get(user.id)

client.identity.user.disable(user.id)
client.identity.user.enable(user.id)
client.identity.user.disable_totp(user.id)
client.identity.user.enable_totp(user.id)
client.identity.user.update_policies(user.id, [policy_id])
client.identity.user.change_email(user.id, "updated@example.com")
client.identity.user.delete(user.id)
```

### User keys

```python
from aprilflow import UserKeyListRequest

raw_user_key = client.identity.user_key.create()

user_keys = client.identity.user_key.list(
    UserKeyListRequest(first_result=0, max_result=20)
)

count = client.identity.user_key.count()

revoked = client.identity.user_key.revoke(key_id)

client.identity.user_key.delete(key_id)
```

## Billing

Billing APIs are available through:

```python
client.billing
```

### Quota types and usage

```python
quota_types = client.billing.quota_type.list()
usages = client.billing.usage.list()
```

### Quotas

```python
from aprilflow import Quota, QuotaListRequest, QuotaScope, QuotaTarget

quota = client.billing.quota.insert(
    Quota(
        tenant_id=tenant.id,
        quota_type_id=quota_type.id,
        usage_id=usage.id,
        quota_scope=QuotaScope.TENANT,
        quota_target=QuotaTarget.TENANT,
        quota_window=1,
        description="SDK quota",
        value=1000,
    )
)

count = client.billing.quota.count()

quotas = client.billing.quota.list(
    QuotaListRequest(first_result=0, max_result=20)
)

quota.description = "Updated SDK quota"
quota.value = 2000
quota = client.billing.quota.update(quota)

client.billing.quota.delete(quota.id)
```

## Messages

Support thread operations are available through:

```python
client.message.support_thread
```

Create a support thread:

```python
from aprilflow import CreateSupportThreadRequest

thread_id = client.message.support_thread.create(
    CreateSupportThreadRequest(
        topic="SDK question",
        email="user@example.com",
        message="Hello from the Python SDK",
    )
)
```

You can also use keyword arguments:

```python
thread_id = client.message.support_thread.create(
    topic="SDK question",
    email="user@example.com",
    message="Hello from the Python SDK",
)
```

## API summary

### Top-level client

| Attribute / Method | Description |
|---|---|
| `AprilFlowClient.create(base_url, user_key)` | Creates a client using default configuration. |
| `collection` | Collection API. |
| `upload` | Upload API. |
| `document` | Document API. |
| `prompt` | Prompt/session API. |
| `identity` | Identity API. |
| `billing` | Billing API. |
| `message` | Message API. |
| `notification` | Notification API. |
| `close()` | Closes the underlying HTTP client. |

### Collection API

| Method | Description |
|---|---|
| `create(request=None, **kwargs)` | Creates a collection. |
| `update(request=None, **kwargs)` | Updates collection metadata. |
| `list(request=None, **kwargs)` | Lists collections. |
| `count(request=None, **kwargs)` | Counts collections. |
| `list_by_permissions()` | Lists collections available through permissions. |
| `set_visibility(request=None, **kwargs)` | Updates visibility and optional policies. |
| `list_policy_ids(collection_id)` | Lists policy IDs assigned to a collection. |
| `delete(collection_id)` | Deletes a collection. |
| `count_deleted()` | Counts deleted collections. |
| `list_deleted(first_result=None, max_result=None)` | Lists deleted collections. |
| `restore(collection_ids)` | Restores deleted collections. |
| `get_user_prompt_collection()` | Gets the user prompt collection. |
| `create_user_prompt_collection()` | Creates the user prompt collection. |

### Upload API

| Method | Description |
|---|---|
| `upload_bytes(collection_id, data, file_name, content_type=None)` | Uploads bytes. |
| `upload_file(collection_id, file, file_name=None, content_type=None)` | Uploads a file from a path. |
| `upload_stream(collection_id, stream, file_name, content_type=None)` | Uploads a stream. |
| `upload_file_and_wait(collection_id, file, file_name=None, content_type=None, wait_options=None)` | Uploads a file and waits for a terminal upload notification. |
| `upload_bytes_and_wait(collection_id, data, file_name, content_type=None, wait_options=None)` | Uploads bytes and waits for a terminal upload notification. |
| `upload_stream_and_wait(collection_id, stream, file_name, content_type=None, wait_options=None)` | Uploads a stream and waits for a terminal upload notification. |
| `watch(collection_id, upload_id, listener, error_listener=None)` | Watches notifications for one upload. |
| `wait_for_upload(upload, wait_options=None)` | Waits for an existing upload to reach a terminal status. |
| `list(request=None, **kwargs)` | Lists uploads. |
| `count(request=None, **kwargs)` | Counts uploads. |
| `batch(collection_id, upload_ids)` | Fetches uploads by ID. |
| `cancel(collection_id, upload_id)` | Cancels processing. |
| `restart(collection_id, upload_id)` | Restarts processing. |
| `delete_document(collection_id, upload_id)` | Deletes generated document content. |
| `delete(collection_id, upload_id, delete_document=False)` | Deletes an upload. |

### Document API

| Method | Description |
|---|---|
| `search(request=None, **kwargs)` | Searches documents in a collection. |
| `batch_items(document_ids)` | Fetches document items by IDs. |
| `get_object_as_string(collection_id, document_id)` | Reads document object as a string. |
| `get_object_as_bytes(collection_id, document_id)` | Reads document object as bytes. |
| `get_original_document(collection_id, document_id)` | Gets original document metadata. |
| `delete_original_document(collection_id, document_id)` | Deletes original document content. |
| `delete(collection_id, document_id)` | Deletes a document. |

### Prompt/session API

| Method | Description |
|---|---|
| `prompt.built_with()` | Returns backend build information. |
| `prompt.list_user_collections()` | Lists user prompt collections. |
| `prompt.set_user_collections(collection_ids)` | Sets user prompt collections. |
| `prompt.list_by_session(session_id)` | Lists prompts for a session. |
| `prompt.restart(prompt_id)` | Restarts a prompt. |
| `prompt.interrupt(prompt_id)` | Interrupts a prompt. |
| `prompt.delete(prompt_id)` | Deletes a prompt. |
| `prompt.session.create(request=None, **kwargs)` | Creates a prompt session. |
| `prompt.session.create_and_wait(request=None, wait_options=None, **kwargs)` | Creates a prompt session and waits for the prompt to reach a terminal status. |
| `prompt.session.update(session_id, request=None, **kwargs)` | Adds a prompt to a session. |
| `prompt.session.update_and_wait(session_id, request=None, wait_options=None, **kwargs)` | Adds a prompt and waits for it to reach a terminal status. |
| `prompt.session.wait_for_completion(prompt, wait_options=None)` | Waits for an existing prompt to reach a terminal status. |
| `prompt.session.update_title(session_id, title)` | Updates session title. |
| `prompt.session.duplicate(session_id, title)` | Duplicates a session. |
| `prompt.session.count()` | Counts sessions. |
| `prompt.session.list(request=None, **kwargs)` | Lists sessions. |
| `prompt.session.delete(session_id)` | Deletes a session. |

### Notification API

| Method | Description |
|---|---|
| `path(object_types=None)` | Builds the notification stream path with optional object type filters. |
| `listen(object_types=None, listener=None, error_listener=None, reconnect=True, reconnect_delay=1.0)` | Opens the authenticated SSE notification stream and invokes callbacks. |

### Identity API

#### Tenant

| Method | Description |
|---|---|
| `client.identity.tenant.get()` | Returns the current tenant. |

#### Countries

| Method | Description |
|---|---|
| `client.identity.country.list()` | Lists available countries. |

#### Policies

| Method | Description |
|---|---|
| `client.identity.policy.count(request=None, **kwargs)` | Counts policies. |
| `client.identity.policy.list(request=None, **kwargs)` | Lists policies. |
| `client.identity.policy.insert(policy)` | Creates a policy. |
| `client.identity.policy.update(policy)` | Updates a policy. |
| `client.identity.policy.delete(policy_id)` | Deletes a policy. |
| `client.identity.policy.list_users(policy_id)` | Lists users assigned to a policy. |
| `client.identity.policy.remove_users(policy_id, user_ids)` | Removes users from a policy. |

#### Users

| Method | Description |
|---|---|
| `client.identity.user.get(user_id)` | Returns one user by ID. |
| `client.identity.user.count(request=None, **kwargs)` | Counts users. |
| `client.identity.user.list(request=None, **kwargs)` | Lists users. |
| `client.identity.user.invite(email, policy_ids)` | Invites a user and assigns policies. |
| `client.identity.user.update_info(user)` | Updates user information. |
| `client.identity.user.delete(user_id)` | Deletes a user. |
| `client.identity.user.update_policies(user_id, policy_ids)` | Replaces policies assigned to a user. |
| `client.identity.user.set_enabled(user_id, enabled)` | Enables or disables a user. |
| `client.identity.user.enable(user_id)` | Enables a user. |
| `client.identity.user.disable(user_id)` | Disables a user. |
| `client.identity.user.set_totp_enabled(user_id, enabled)` | Enables or disables TOTP for a user. |
| `client.identity.user.enable_totp(user_id)` | Enables TOTP. |
| `client.identity.user.disable_totp(user_id)` | Disables TOTP. |
| `client.identity.user.change_email(user_id, email)` | Changes a user email address. |

#### User keys

| Method | Description |
|---|---|
| `client.identity.user_key.count()` | Counts user keys. |
| `client.identity.user_key.list(request=None, **kwargs)` | Lists user keys. |
| `client.identity.user_key.create()` | Creates a new user key and returns the key value. |
| `client.identity.user_key.revoke(key_id)` | Revokes a user key. |
| `client.identity.user_key.delete(key_id_or_user_key)` | Deletes a user key. |

### Billing API

#### Quota types

| Method | Description |
|---|---|
| `client.billing.quota_type.list()` | Lists available quota types. |

#### Usage definitions

| Method | Description |
|---|---|
| `client.billing.usage.list()` | Lists available usage definitions. |

#### Quotas

| Method | Description |
|---|---|
| `client.billing.quota.count()` | Counts quotas. |
| `client.billing.quota.list(request=None, **kwargs)` | Lists quotas. |
| `client.billing.quota.insert(quota)` | Creates a quota. |
| `client.billing.quota.update(quota)` | Updates a quota. |
| `client.billing.quota.delete(quota_id)` | Deletes a quota. |

### Message API

#### Support threads

| Method | Description |
|---|---|
| `client.message.support_thread.create(request=None, **kwargs)` | Creates a support thread and returns the response body as a string. |

## Java to Python naming guide

| Java SDK | Python SDK |
|---|---|
| `client.collection()` | `client.collection` |
| `client.upload()` | `client.upload` |
| `client.document()` | `client.document` |
| `client.prompt()` | `client.prompt` |
| `client.prompt().session()` | `client.prompt.session` |
| `client.identity().userKey()` | `client.identity.user_key` |
| `client.collection().setVisibility(...)` | `client.collection.set_visibility(...)` |
| `client.document().getObjectAsString(...)` | `client.document.get_object_as_string(...)` |
| `CollectionVisibility.Private` | `CollectionVisibility.PRIVATE` |
| `UploadStatus.Processed` | `UploadStatus.PROCESSED` |


## Tests

The SDK includes both fast offline tests and live integration tests. See [`TESTING.md`](TESTING.md) for the full guide.

Install the package with test dependencies:

```bash
python -m pip install -e ".[test]"
```

Run the complete local test command. Without credentials, live integration tests are skipped automatically:

```bash
pytest
```

Run only the offline tests:

```bash
pytest tests --ignore=tests/integration
```

Run live tests against an AprilFlow environment:

```bash
export APRILFLOW_BASE_URL=https://api.aprilflow.ai
export APRILFLOW_USER_KEY=your-user-key
pytest -m integration
```

The test suite covers:

- typed model parsing and API alias serialization;
- JWT parsing and user-key token provider caching;
- REST authentication, query parameters, path encoding, and error handling;
- resource endpoint wiring and payload generation;
- SSE notification parsing and notification-driven wait helpers;
- live collection, document, upload, prompt/session, identity, billing, message, and user-key flows when credentials are provided.
