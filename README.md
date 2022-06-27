# MM-cat

A multi-model data modelling framework based on the category theory. It consists of the following modules:
- [Core](./core/README.md) defines classes needed by other modules.
- [Transformations](./transformations/README.md) contains algorithms for transforming data from logical to conceptual model (and back).
- [Wrappers](./wrappers/README.md) provides funcionalities specific for different databases.
- [Evolution](./evolution/README.md) will be implemented later and it will include algorithms for the evolution of both the conceptual and the logical models.
- [Backend application](./server/README.md) provides an API which exposes the functionality of the other modules. It also contains a job scheduler for the transformation algorithms.
- [Frontend application](./example-ui//README.md) is used as a UI for the backend API. It is also a tool for modelling all the necessary data structures which are then used by the other modules.

## Installation

- Make sure you have all the required software for both [Backend](./server/README.md#requirements) and [Frontend](./example-ui/README.md#configuration) applications.
- To compile the java modules and install dependencies, run:
```sh
mvn install -Dmaven.test.skip
```
- Then follow the steps in the [Backend](./server/README.md) and [Frontend](./example-ui/README.md) guides.
- Lastly, you need a web server to make both applications available. See the [Apache configuration](./documentation/apacheConfiguration.md) for detailed instructions.
