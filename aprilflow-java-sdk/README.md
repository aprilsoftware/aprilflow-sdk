# AprilFlow Java SDK

Java 17 SDK for calling AprilFlow APIs from server-side or programmatic Java applications.

The SDK provides typed Java APIs for collections, uploads, documents, prompts, identity, billing, support messages, and server-sent notification streams. It uses Jersey for HTTP, multipart upload, JSON integration, and SSE notification handling.

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

- Java 17+
- Maven
- An AprilFlow base URL
- An AprilFlow user key

The project is built as a Maven JAR:

```xml
<groupId>com.aprilsoftware</groupId>
  <artifactId>aprilflow-java-sdk</artifactId>
<version>1.0</version>
```

## Installation

When the SDK is available from your Maven repository, add it to your application:

```xml
<dependency>
    <groupId>com.aprilsoftware</groupId>
    <artifactId>aprilflow-java-sdk</artifactId>
    <version>1.0</version>
</dependency>
```

For local development, install it into your local Maven repository:

```bash
mvn clean install
```

## Creating a client

The simplest way to create a client is with `AprilFlowClient.create(...)`:

```java
import aprilflow.sdk.AprilFlowClient;

public final class Example
{
    public static void main(String[] args)
    {
        AprilFlowClient client;

        client = AprilFlowClient.create(
            "https://api.aprilflow.ai",
            System.getenv("APRILFLOW_USER_KEY")
        );

        System.out.println(client.prompt().builtWith());
    }
}
```

The equivalent builder form is:

```java
AprilFlowClient client;

client = AprilFlowClient.builder()
    .baseUrl("https://api.aprilflow.ai")
    .userKey(System.getenv("APRILFLOW_USER_KEY"))
    .build();
```

The top-level client exposes the following API groups:

```java
client.collection();
client.upload();
client.document();
client.prompt();
client.identity();
client.billing();
client.message();
client.notification();
```

## Authentication

The SDK authenticates with a user key. The builder creates a `UserKeyTokenProvider`, which obtains access tokens and uses bearer authentication for HTTP and notification requests.

Keep user keys on trusted server-side systems only. Do not embed them in browser or mobile applications.

## Error handling

The SDK uses runtime exceptions.

Typical exceptions include:

- `IllegalArgumentException` for invalid request parameters.
- `AprilFlowException` for API errors, authentication errors, notification errors, and wait timeouts.
- Backend validation or authorization failures are surfaced through response handling.

Example:

```java
import aprilflow.sdk.AprilFlowException;

try
{
    String version;

    version = client.prompt().builtWith();

    System.out.println(version);
}
catch (AprilFlowException exception)
{
    System.err.println(exception.getMessage());
}
```

## Collections

Collections group uploaded content and can be used by prompts.

### Create a collection

```java
import aprilflow.sdk.collection.Collection;
import aprilflow.sdk.collection.CollectionVisibility;
import aprilflow.sdk.collection.CreateCollectionRequest;

Collection collection;

collection = client.collection().create(
    CreateCollectionRequest.create()
        .name("SDK example collection")
        .description("Collection created from the Java SDK")
        .visibility(CollectionVisibility.Private)
);

System.out.println(collection.getId());
```

### List and count collections

```java
import aprilflow.sdk.collection.Collection;
import aprilflow.sdk.collection.CollectionListRequest;

int count;
java.util.List<Collection> collections;

count = client.collection().count(
    CollectionListRequest.create()
        .search("SDK")
);

collections = client.collection().list(
    CollectionListRequest.create()
        .search("SDK")
        .firstResult(0)
        .maxResult(20)
);
```

### Update a collection

```java
import aprilflow.sdk.collection.Collection;
import aprilflow.sdk.collection.UpdateCollectionRequest;

Collection updated;

updated = client.collection().update(
    UpdateCollectionRequest.create()
        .collectionId(collection.getId())
        .name("Updated collection name")
        .description("Updated from the Java SDK")
);
```

### Set collection visibility

```java
import aprilflow.sdk.collection.Collection;
import aprilflow.sdk.collection.CollectionVisibility;
import aprilflow.sdk.collection.SetCollectionVisibilityRequest;

Collection updated;

updated = client.collection().setVisibility(
    SetCollectionVisibilityRequest.create()
        .collectionId(collection.getId())
        .visibility(CollectionVisibility.Public)
);
```

For restricted collections, pass policy IDs:

```java
updated = client.collection().setVisibility(
    SetCollectionVisibilityRequest.create()
        .collectionId(collection.getId())
        .visibility(CollectionVisibility.Restricted)
        .policyIds(java.util.List.of(policyId))
);
```

### Deleted collections

```java
client.collection().delete(collection.getId());

int deletedCount;
java.util.List<Collection> deletedCollections;
java.util.List<Collection> restored;

deletedCount = client.collection().countDeleted();

deletedCollections = client.collection().listDeleted(0, 20);

restored = client.collection().restore(java.util.List.of(collection.getId()));
```

### User prompt collection

```java
Collection userPromptCollection;

userPromptCollection = client.collection().getUserPromptCollection();

if (userPromptCollection == null)
{
    userPromptCollection = client.collection().createUserPromptCollection();
}
```

### Other collection operations

```java
client.collection().listByPermissions();
client.collection().listPolicyIds(collectionId);
client.collection().delete(collectionId);
```

## Uploads

The upload API supports uploads from:

- `byte[]`
- `Path`
- `Supplier<InputStream>`

It also supports notification-driven wait helpers for upload processing.

Upload processing notifications are expected to use:

```text
action     = upload.process
objectType = collection.upload
```

The notification object is deserialized into `Upload` and used as the latest upload state.

### Upload bytes

```java
import aprilflow.sdk.upload.Upload;
import aprilflow.sdk.upload.UploadBytesRequest;

import java.nio.charset.StandardCharsets;

Upload upload;

upload = client.upload().uploadBytes(
    UploadBytesRequest.create()
        .collectionId(collection.getId())
        .fileName("example.txt")
        .contentType("text/plain")
        .bytes("Hello from the SDK".getBytes(StandardCharsets.UTF_8))
);
```

### Upload bytes and wait

```java
import aprilflow.sdk.upload.Upload;
import aprilflow.sdk.upload.UploadBytesRequest;
import aprilflow.sdk.upload.UploadStatus;
import aprilflow.sdk.upload.UploadWaitOptions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

Upload upload;

upload = client.upload().uploadBytesAndWait(
    UploadBytesRequest.create()
        .collectionId(collection.getId())
        .fileName("example.txt")
        .contentType("text/plain")
        .bytes("Hello from the SDK".getBytes(StandardCharsets.UTF_8)),
    new UploadWaitOptions(
        Duration.ofMinutes(3),
        List.of(
            UploadStatus.Processed,
            UploadStatus.Ignored,
            UploadStatus.OnError,
            UploadStatus.QuotaExceeded
        )
    )
);

System.out.println(upload.getStatus());
```

### Upload a file

```java
import aprilflow.sdk.upload.Upload;
import aprilflow.sdk.upload.UploadFileRequest;

import java.nio.file.Path;

Upload upload;

upload = client.upload().uploadFile(
    UploadFileRequest.create()
        .collectionId(collection.getId())
        .file(Path.of("/path/to/document.pdf"))
        .fileName("document.pdf")
        .contentType("application/pdf")
);
```

### Upload a file and wait

```java
Upload upload;

upload = client.upload().uploadFileAndWait(
    UploadFileRequest.create()
        .collectionId(collection.getId())
        .file(Path.of("/path/to/document.pdf"))
        .fileName("document.pdf")
        .contentType("application/pdf"),
    UploadWaitOptions.defaultOptions()
);
```

### Upload a stream and wait

```java
import aprilflow.sdk.upload.Upload;
import aprilflow.sdk.upload.UploadStreamRequest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

byte[] bytes;
Upload upload;

bytes = "Stream content".getBytes(StandardCharsets.UTF_8);

upload = client.upload().uploadStreamAndWait(
    UploadStreamRequest.create()
        .collectionId(collection.getId())
        .fileName("stream.txt")
        .contentType("text/plain")
        .inputStream(() -> new ByteArrayInputStream(bytes)),
    UploadWaitOptions.defaultOptions()
);
```

### List, count, and batch uploads

```java
import aprilflow.sdk.upload.Upload;
import aprilflow.sdk.upload.UploadListRequest;
import aprilflow.sdk.upload.UploadStatus;

int count;
java.util.List<Upload> uploads;
java.util.List<Upload> processedUploads;

uploads = client.upload().list(
    UploadListRequest.create()
        .collectionId(collection.getId())
        .firstResult(0)
        .maxResult(20)
);

processedUploads = client.upload().list(
    UploadListRequest.create()
        .collectionId(collection.getId())
        .status(UploadStatus.Processed)
        .firstResult(0)
        .maxResult(20)
);

count = client.upload().count(
    UploadListRequest.create()
        .collectionId(collection.getId())
);
```

Batch lookup:

```java
import aprilflow.sdk.upload.UploadBatchRequest;

java.util.List<Upload> batch;

batch = client.upload().batch(
    UploadBatchRequest.create()
        .collectionId(collection.getId())
        .uploadIds(java.util.List.of(upload.getId()))
);
```

### Upload lifecycle operations

```java
Upload canceled;
Upload restarted;

canceled = client.upload().cancel(collectionId, uploadId);
restarted = client.upload().restart(collectionId, uploadId);

client.upload().deleteDocument(collectionId, uploadId);
client.upload().delete(collectionId, uploadId, true);
```

### Watch an upload

```java
import aprilflow.sdk.notification.NotificationSubscription;

NotificationSubscription subscription;

subscription = client.upload().watch(
    collection.getId(),
    upload.getId(),
    notification -> {
        System.out.println(notification.getAction());
        System.out.println(notification.getObjectType());
        System.out.println(notification.getObject());
    },
    error -> {
        error.printStackTrace();
    }
);

// Close when no longer needed.
subscription.close();
```

## Documents

The document API provides document search and document object retrieval.

### Search documents in a collection

```java
import aprilflow.sdk.document.DocumentItem;
import aprilflow.sdk.document.DocumentSearchRequest;

java.util.List<DocumentItem> documents;

documents = client.document().search(
    DocumentSearchRequest.create()
        .collectionId(collection.getId())
        .text("search text")
        .maxResult(10)
);
```

### Batch document items

```java
import aprilflow.sdk.document.DocumentId;
import aprilflow.sdk.document.DocumentItem;

java.util.List<DocumentItem> items;

items = client.document().batchItems(
    java.util.List.of(
        DocumentId.of(collectionId, documentId)
    )
);
```

### Retrieve document objects

```java
String objectAsString;
byte[] objectAsBytes;

objectAsString = client.document().getObjectAsString(collectionId, documentId);
objectAsBytes = client.document().getObjectAsBytes(collectionId, documentId);
```

### Original document operations

```java
import aprilflow.sdk.document.Document;

Document original;

original = client.document().getOriginalDocument(collectionId, documentId);

client.document().deleteOriginalDocument(collectionId, documentId);
```

### Delete a document

```java
client.document().delete(collectionId, documentId);
```

## Prompts and sessions

Prompt operations are accessed through:

```java
client.prompt()
```

Session operations are accessed through:

```java
client.prompt().session()
```

Prompt processing notifications are expected to use:

```text
action     = prompt.process
objectType = prompt
```

The notification object is deserialized into `Prompt` and used as the latest prompt state.

### Create a prompt session

```java
import aprilflow.sdk.prompt.CreateSessionResult;
import aprilflow.sdk.prompt.PromptRequest;

CreateSessionResult result;

result = client.prompt().session().create(
    PromptRequest.create()
        .text("Please tell me a short story.")
);

System.out.println(result.getSession().getId());
System.out.println(result.getPrompt().getId());
System.out.println(result.getPrompt().getOutput());
```

### Create a prompt session and wait

```java
import aprilflow.sdk.prompt.CreateSessionResult;
import aprilflow.sdk.prompt.PromptRequest;
import aprilflow.sdk.prompt.PromptStatus;
import aprilflow.sdk.prompt.PromptWaitOptions;

import java.time.Duration;
import java.util.List;

CreateSessionResult result;

result = client.prompt().session().createAndWait(
    PromptRequest.create()
        .text("Please tell me a short story."),
    new PromptWaitOptions(
        Duration.ofMinutes(3),
        List.of(
            PromptStatus.Completed,
            PromptStatus.Interrupted,
            PromptStatus.OnError,
            PromptStatus.QuotaExceeded
        )
    )
);

System.out.println(result.getPrompt().getStatus());
System.out.println(result.getPrompt().getOutput());
```

### Create a prompt using collections or uploads

```java
CreateSessionResult result;

result = client.prompt().session().createAndWait(
    PromptRequest.create()
        .text("Summarize the uploaded content.")
        .collectionId(collection.getId())
        .uploadId(collection.getId(), upload.getId()),
    PromptWaitOptions.defaultOptions()
);
```

### Update an existing session

```java
import aprilflow.sdk.prompt.Prompt;

Prompt prompt;

prompt = client.prompt().session().update(
    sessionId,
    PromptRequest.create()
        .text("Continue the answer.")
);
```

### Update an existing session and wait

```java
Prompt prompt;

prompt = client.prompt().session().updateAndWait(
    sessionId,
    PromptRequest.create()
        .text("Continue the answer."),
    PromptWaitOptions.defaultOptions()
);
```

### Session management

```java
import aprilflow.sdk.prompt.Session;
import aprilflow.sdk.prompt.SessionListRequest;

Session updated;
Session duplicated;
int count;
java.util.List<Session> sessions;

updated = client.prompt().session().updateTitle(sessionId, "New title");

duplicated = client.prompt().session().duplicate(sessionId, "Copied session");

count = client.prompt().session().count();

sessions = client.prompt().session().list(
    SessionListRequest.create()
        .firstResult(0)
        .maxResult(20)
);

client.prompt().session().delete(sessionId);
```

### Prompt operations

```java
import aprilflow.sdk.prompt.Prompt;

java.util.List<Prompt> prompts;
Prompt restarted;
Prompt interrupted;

prompts = client.prompt().listBySession(sessionId);

restarted = client.prompt().restart(promptId);

interrupted = client.prompt().interrupt(promptId);

client.prompt().delete(promptId);
```

### User prompt collections

```java
import aprilflow.sdk.prompt.UserCollection;

java.util.List<UserCollection> collections;

collections = client.prompt().listUserCollections();

client.prompt().setUserCollections(java.util.List.of(collectionId));
```

### Backend information

```java
String builtWith;

builtWith = client.prompt().builtWith();
```

## Notifications

The notification API provides direct access to the SSE notification stream.

Most applications should prefer the higher-level wait helpers:

```java
client.upload().uploadBytesAndWait(...);
client.upload().uploadFileAndWait(...);
client.upload().uploadStreamAndWait(...);
client.prompt().session().createAndWait(...);
client.prompt().session().updateAndWait(...);
```

Direct subscription is available when needed:

```java
import aprilflow.sdk.notification.NotificationSubscription;

NotificationSubscription subscription;

subscription = client.notification().listen(
    java.util.List.of("prompt"),
    notification -> {
        System.out.println(notification.getAction());
        System.out.println(notification.getObjectType());
        System.out.println(notification.getObject());
    },
    error -> {
        error.printStackTrace();
    }
);

subscription.close();
```

Listen to all notifications:

```java
NotificationSubscription subscription;

subscription = client.notification().listen(
    notification -> System.out.println(notification.getObjectType())
);
```

The returned `NotificationSubscription` implements `AutoCloseable`:

```java
try (NotificationSubscription subscription = client.notification().listen(notification -> {
    System.out.println(notification.getAction());
}))
{
    Thread.sleep(30_000);
}
```

## Identity

Identity APIs are available through:

```java
client.identity()
```

### Tenant and countries

```java
import aprilflow.sdk.identity.Country;
import aprilflow.sdk.identity.Tenant;

Tenant tenant;
java.util.List<Country> countries;

tenant = client.identity().tenant().get();

countries = client.identity().country().list();
```

### Policies

```java
import aprilflow.sdk.identity.Policy;
import aprilflow.sdk.identity.PolicyListRequest;
import aprilflow.sdk.identity.TenantRole;

Policy policy;

policy = client.identity().policy().insert(
    Policy.create()
        .name("SDK policy")
        .description("Policy created from the Java SDK")
        .tenantId(tenant.getId())
        .roles(java.util.List.of(
            TenantRole.PromptCreate,
            TenantRole.PromptRead
        ))
        .build()
);

int count;
java.util.List<Policy> policies;

count = client.identity().policy().count(
    PolicyListRequest.create()
        .search("SDK")
);

policies = client.identity().policy().list(
    PolicyListRequest.create()
        .search("SDK")
        .firstResult(0)
        .maxResult(20)
);

policy = client.identity().policy().update(
    Policy.create()
        .id(policy.getId())
        .name("Updated SDK policy")
        .description("Updated policy")
        .tenantId(tenant.getId())
        .roles(java.util.List.of(TenantRole.PromptRead))
        .build()
);

client.identity().policy().delete(policy.getId());
```

Policy users:

```java
client.identity().policy().listUsers(policyId);
client.identity().policy().removeUsers(policyId, java.util.List.of(userId));
```

### Users

```java
import aprilflow.sdk.identity.User;
import aprilflow.sdk.identity.UserListRequest;

User user;

user = client.identity().user().invite(
    "new-user@example.com",
    java.util.List.of(policyId)
);

java.util.List<User> users;
int count;

count = client.identity().user().count(
    UserListRequest.create()
        .search("new-user@example.com")
);

users = client.identity().user().list(
    UserListRequest.create()
        .search("new-user@example.com")
        .firstResult(0)
        .maxResult(20)
);

user = client.identity().user().get(user.getId());

client.identity().user().disable(user.getId());
client.identity().user().enable(user.getId());
client.identity().user().disableTotp(user.getId());
client.identity().user().enableTotp(user.getId());
client.identity().user().updatePolicies(user.getId(), java.util.List.of(policyId));
client.identity().user().changeEmail(user.getId(), "updated@example.com");
client.identity().user().delete(user.getId());
```

### User keys

```java
import aprilflow.sdk.identity.UserKey;
import aprilflow.sdk.identity.UserKeyListRequest;

String rawUserKey;
java.util.List<UserKey> userKeys;
UserKey revoked;
int count;

rawUserKey = client.identity().userKey().create();

userKeys = client.identity().userKey().list(
    UserKeyListRequest.create()
        .firstResult(0)
        .maxResult(20)
);

count = client.identity().userKey().count();

revoked = client.identity().userKey().revoke(keyId);

client.identity().userKey().delete(keyId);
```

## Billing

Billing APIs are available through:

```java
client.billing()
```

### Quota types and usage

```java
import aprilflow.sdk.billing.QuotaType;
import aprilflow.sdk.billing.Usage;

java.util.List<QuotaType> quotaTypes;
java.util.List<Usage> usages;

quotaTypes = client.billing().quotaType().list();

usages = client.billing().usage().list();
```

### Quotas

```java
import aprilflow.sdk.billing.Quota;
import aprilflow.sdk.billing.QuotaListRequest;
import aprilflow.sdk.billing.QuotaScope;
import aprilflow.sdk.billing.QuotaTarget;

Quota quota;

quota = client.billing().quota().insert(
    Quota.create()
        .tenantId(tenant.getId())
        .quotaTypeId(quotaType.getId())
        .usageId(usage.getId())
        .quotaScope(QuotaScope.Tenant)
        .quotaTarget(QuotaTarget.Tenant)
        .quotaWindow(1)
        .description("SDK quota")
        .value(1000)
        .build()
);

int count;
java.util.List<Quota> quotas;

count = client.billing().quota().count();

quotas = client.billing().quota().list(
    QuotaListRequest.create()
        .firstResult(0)
        .maxResult(20)
);

quota = client.billing().quota().update(
    Quota.create()
        .id(quota.getId())
        .tenantId(tenant.getId())
        .quotaTypeId(quotaType.getId())
        .usageId(usage.getId())
        .quotaScope(QuotaScope.Tenant)
        .quotaTarget(QuotaTarget.Tenant)
        .quotaWindow(1)
        .description("Updated SDK quota")
        .value(2000)
        .build()
);

client.billing().quota().delete(quota.getId());
```

## Messages

Support thread operations are available through:

```java
client.message().supportThread()
```

Create a support thread:

```java
import aprilflow.sdk.message.CreateSupportThreadRequest;

String threadId;

threadId = client.message().supportThread().create(
    CreateSupportThreadRequest.create()
        .topic("SDK question")
        .email("user@example.com")
        .message("Hello from the Java SDK")
);
```

## Custom clients and serialization

The builder accepts custom components:

```java
AprilFlowClient client;

client = AprilFlowClient.builder()
    .baseUrl("https://api.aprilflow.ai")
    .userKey(userKey)
    .httpClient(customHttpClient)
    .notificationClient(customNotificationClient)
    .jsonSerializer(customJsonSerializer)
    .build();
```

Custom HTTP clients implement:

```java
aprilflow.sdk.http.AprilFlowHttpClient
```

Custom notification clients implement:

```java
aprilflow.sdk.notification.AprilFlowNotificationClient
```

Custom JSON serializers implement:

```java
aprilflow.sdk.json.JsonSerializer
```

## API summary

### Top-level client

| Method | Description |
|---|---|
| `AprilFlowClient.create(baseUrl, userKey)` | Creates a client using the default builder. |
| `AprilFlowClient.builder()` | Creates a configurable client builder. |
| `collection()` | Collection API. |
| `upload()` | Upload API. |
| `document()` | Document API. |
| `prompt()` | Prompt/session API. |
| `identity()` | Identity API. |
| `billing()` | Billing API. |
| `message()` | Message API. |
| `notification()` | Notification API. |

### Collection API

| Method | Description |
|---|---|
| `create(CreateCollectionRequest)` | Creates a collection. |
| `update(UpdateCollectionRequest)` | Updates collection metadata. |
| `list(CollectionListRequest)` | Lists collections. |
| `count(CollectionListRequest)` | Counts collections. |
| `listByPermissions()` | Lists collections available through permissions. |
| `setVisibility(SetCollectionVisibilityRequest)` | Updates visibility and optional policies. |
| `listPolicyIds(collectionId)` | Lists policy IDs assigned to a collection. |
| `delete(collectionId)` | Deletes a collection. |
| `countDeleted()` | Counts deleted collections. |
| `listDeleted(firstResult, maxResult)` | Lists deleted collections. |
| `restore(collectionIds)` | Restores deleted collections. |
| `getUserPromptCollection()` | Gets the user prompt collection. |
| `createUserPromptCollection()` | Creates the user prompt collection. |

### Upload API

| Method | Description |
|---|---|
| `uploadBytes(UploadBytesRequest)` | Uploads bytes. |
| `uploadFile(UploadFileRequest)` | Uploads a file from a path. |
| `uploadStream(UploadStreamRequest)` | Uploads a stream. |
| `uploadBytesAndWait(...)` | Uploads bytes and waits for terminal processing status. |
| `uploadFileAndWait(...)` | Uploads a file and waits for terminal processing status. |
| `uploadStreamAndWait(...)` | Uploads a stream and waits for terminal processing status. |
| `list(UploadListRequest)` | Lists uploads. |
| `count(UploadListRequest)` | Counts uploads. |
| `batch(UploadBatchRequest)` | Fetches uploads by ID. |
| `cancel(collectionId, uploadId)` | Cancels processing. |
| `restart(collectionId, uploadId)` | Restarts processing. |
| `deleteDocument(collectionId, uploadId)` | Deletes generated document content. |
| `delete(collectionId, uploadId, deleteDocument)` | Deletes an upload. |
| `watch(collectionId, uploadId, ...)` | Watches upload notifications. |

### Document API

| Method | Description |
|---|---|
| `search(DocumentSearchRequest)` | Searches documents in a collection. |
| `batchItems(documentIds)` | Fetches document items by IDs. |
| `getObjectAsString(collectionId, documentId)` | Reads document object as a string. |
| `getObjectAsBytes(collectionId, documentId)` | Reads document object as bytes. |
| `getOriginalDocument(collectionId, documentId)` | Gets original document metadata. |
| `deleteOriginalDocument(collectionId, documentId)` | Deletes original document content. |
| `delete(collectionId, documentId)` | Deletes a document. |

### Prompt/session API

| Method | Description |
|---|---|
| `prompt().builtWith()` | Returns backend build information. |
| `prompt().listUserCollections()` | Lists user prompt collections. |
| `prompt().setUserCollections(collectionIds)` | Sets user prompt collections. |
| `prompt().listBySession(sessionId)` | Lists prompts for a session. |
| `prompt().restart(promptId)` | Restarts a prompt. |
| `prompt().interrupt(promptId)` | Interrupts a prompt. |
| `prompt().delete(promptId)` | Deletes a prompt. |
| `prompt().session().create(request)` | Creates a prompt session. |
| `prompt().session().createAndWait(request, options)` | Creates a prompt session and waits for terminal prompt status. |
| `prompt().session().update(sessionId, request)` | Adds a prompt to a session. |
| `prompt().session().updateAndWait(sessionId, request, options)` | Adds a prompt and waits for terminal status. |
| `prompt().session().updateTitle(sessionId, title)` | Updates session title. |
| `prompt().session().duplicate(sessionId, title)` | Duplicates a session. |
| `prompt().session().count()` | Counts sessions. |
| `prompt().session().list(request)` | Lists sessions. |
| `prompt().session().delete(sessionId)` | Deletes a session. |

### Notification API

| Method | Description |
|---|---|
| `listen(listener)` | Listens to all notifications. |
| `listen(listener, errorListener)` | Listens to all notifications with error callback. |
| `listen(objectTypes, listener)` | Listens to selected object types. |
| `listen(objectTypes, listener, errorListener)` | Listens to selected object types with error callback. |

### Identity API

#### Tenant

| Method | Description |
|---|---|
| `client.identity().tenant().get()` | Returns the current tenant. |

#### Countries

| Method | Description |
|---|---|
| `client.identity().country().list()` | Lists available countries. |

#### Policies

| Method | Description |
|---|---|
| `client.identity().policy().count(request)` | Counts policies matching a `PolicyListRequest`. |
| `client.identity().policy().list(request)` | Lists policies with optional pagination and search. |
| `client.identity().policy().insert(policy)` | Creates a policy. |
| `client.identity().policy().update(policy)` | Updates a policy. |
| `client.identity().policy().delete(policyId)` | Deletes a policy. |
| `client.identity().policy().listUsers(policyId)` | Lists users assigned to a policy. |
| `client.identity().policy().removeUsers(policyId, userIds)` | Removes users from a policy. |

#### Users

| Method | Description |
|---|---|
| `client.identity().user().get(userId)` | Returns one user by id. |
| `client.identity().user().count(request)` | Counts users matching a `UserListRequest`. |
| `client.identity().user().list(request)` | Lists users with optional search and pagination. |
| `client.identity().user().invite(email, policyIds)` | Invites a user and assigns policies. |
| `client.identity().user().updateInfo(user)` | Updates user information. |
| `client.identity().user().delete(userId)` | Deletes a user. |
| `client.identity().user().updatePolicies(userId, policyIds)` | Replaces the policies assigned to a user. |
| `client.identity().user().setEnabled(userId, enabled)` | Enables or disables a user. |
| `client.identity().user().enable(userId)` | Enables a user. |
| `client.identity().user().disable(userId)` | Disables a user. |
| `client.identity().user().setTotpEnabled(userId, enabled)` | Enables or disables TOTP for a user. |
| `client.identity().user().enableTotp(userId)` | Enables TOTP for a user. |
| `client.identity().user().disableTotp(userId)` | Disables TOTP for a user. |
| `client.identity().user().changeEmail(userId, email)` | Changes a user email address. |

#### User keys

| Method | Description |
|---|---|
| `client.identity().userKey().count()` | Counts user keys. |
| `client.identity().userKey().list(request)` | Lists user keys with pagination. |
| `client.identity().userKey().create()` | Creates a new user key and returns the key value. |
| `client.identity().userKey().revoke(keyId)` | Revokes a user key. |
| `client.identity().userKey().delete(keyId)` | Deletes a user key by id. |
| `client.identity().userKey().delete(userKey)` | Deletes a user key object. |

### Billing API

#### Quota types

| Method | Description |
|---|---|
| `client.billing().quotaType().list()` | Lists available quota types. |

#### Usage definitions

| Method | Description |
|---|---|
| `client.billing().usage().list()` | Lists available usage definitions. |

#### Quotas

| Method | Description |
|---|---|
| `client.billing().quota().count()` | Counts quotas. |
| `client.billing().quota().list(request)` | Lists quotas with pagination. |
| `client.billing().quota().insert(quota)` | Creates a quota. |
| `client.billing().quota().update(quota)` | Updates a quota. |
| `client.billing().quota().delete(quotaId)` | Deletes a quota. |


### Message API

#### Support threads

| Method | Description |
|---|---|
| `client.message().supportThread().create(request)` | Creates a support thread and returns the response body as a string. |
