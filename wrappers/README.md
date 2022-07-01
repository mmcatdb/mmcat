# wrappers

This package contains specific implementations of the [abstract wrappers](../abstractWrappers/README.md) for selected database system. These currently include:
- MongoDB
- PostgreSQL

Whenever a wrapper is added, it must be also registered by the services of the [server](../server/README.md) application so that it can be used.