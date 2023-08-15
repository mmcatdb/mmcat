# MM-cat library

A multi-model data modeling and evolution framework based on category theory. It consists of the following modules:
- [Core](./library/core) defines classes needed by other modules.
- [Transformations](./library/transformations/README.md) contains algorithms for transforming data from logical to conceptual model (and back).
- [Wrappers](./library/wrappers/README.md) provides funcionalities specific for different databases.
- [Evolution](./library/evolution/README.md) includes algorithms for the evolution of both the conceptual and the logical models.
- [Server](./library/server/README.md) provides an API which exposes the functionality of the other modules. It also contains a job scheduler for the transformation algorithms.

## Requirements

- Java 17 (JDK)
- Apache Maven 3.8

## Installation & configuration

- To compile the java modules and install dependencies, run:
```bash
mvn clean install -DskipTests
```
- Then see the [Server](./library/server/README.md) package.
