# wrappers

This package contains specific implementations of the [abstract wrappers](../abstractwrappers/README.md) for selected database system. These currently include:
- MongoDB
- PostgreSQL
- Neo4j

Whenever a wrapper is added, it must be also registered by the services of the [server](../server/README.md) application so that it can be used.