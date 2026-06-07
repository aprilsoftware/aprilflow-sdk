# April Flow SDK

This repository contains the SDKs and tools used to integrate applications with the [April Flow](https://www.aprilflow.com/) API.

## About April Flow

[April Flow](https://www.aprilflow.com/) is a sovereign AI assistant for documents and data. It helps users ask questions over selected document Collections and receive answers grounded in the right context, with access to the document extracts that support the answer.

April Flow is designed for organizations that need AI to work with their own knowledge while keeping control over where data is stored, searched, and processed. Documents are organized into Collections, and users can select one or more Collections so the assistant knows where to search before replying.

The platform focuses on private knowledge bases, verifiable answers, traceable supporting extracts, and deployment options suitable for organizations with sensitive, technical, legal, operational, or business-critical information.

Useful links:

- [April Flow website](https://www.aprilflow.com/)
- [Sovereign AI deployment](https://www.aprilflow.com/web/sovereign-ai.xhtml)
- [April Software](https://www.aprilsoftware.com/)

## Projects

This repository contains three related projects around the April Flow API:

- **aprilflow-java-sdk** — the production-ready Java SDK for April Flow.
- **aprilflow-python-sdk** — the production-ready Python SDK for April Flow.
- **aprilflow-scraper** — a Python scraper/CLI project using the April Flow Python SDK.

## SDKs

### aprilflow-java-sdk

Java SDK used to connect to April Flow APIs.

Use this project when you need to integrate April Flow from Java applications, backend services, batch jobs, or other JVM-based systems.

Documentation:

- [aprilflow-java-sdk README](./aprilflow-java-sdk/README.md)
- [aprilflow-java-sdk testing documentation](./aprilflow-java-sdk/TESTING.md)

### aprilflow-python-sdk

Python SDK used to connect to April Flow APIs.

Use this project when you need to integrate April Flow from Python applications, scripts, CLI tools, automation workflows, data processing jobs, or services.

Documentation:

- [aprilflow-python-sdk README](./aprilflow-python-sdk/README.md)
- [aprilflow-python-sdk testing documentation](./aprilflow-python-sdk/TESTING.md)

## Tools

### aprilflow-scraper

Python scraper/CLI project that uses the April Flow Python SDK.

Use this project to run scraping workflows and upload or manage data through April Flow.

Documentation:

- [aprilflow-scraper README](./aprilflow-scraper/README.md)


## License

Copyright 2026 April Software

Licensed under the Apache License, Version 2.0. See the license files in each project for details.
