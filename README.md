# MM-cat

A multi-model data modelling tool. It consists of
- a java backend application,
- a typescript frontend application
- and some java modules:
    - core objects needed by other modules,
    - transformation algorithms,
    - wrappers for supported databases.
- There is also the evolution module for the evolution management which will be developed later.

## Installation

- Make sure you have all the required software for both [Backend](./server/README.md#requirements) and [Frontend](./example-ui/README.md#configuration) applications.
- To compile the java modules and install dependencies, run:
```sh
mvn install -Dmaven.test.skip
```
- Then follow the steps in the [Backend](./server/README.md) and [Frontend](./example-ui/README.md) guides.
- Lastly, you need a web server to make both applications available. See the [Apache configuration](./documentation/apacheConfiguration.md) for detailed instructions.
