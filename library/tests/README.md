# tests

Because all parts of the framework depend on the same basic concepts (schema category, transformation algorithms, etc.) most of their tests are here. The main code defines commonly used test data and structures while the test code contains the tests themselves.

The tests for the server package are not included here - on the contrary, the server package depends on this one.

## Configuration

First, you need the test databases. Those should be created by the same docker command that creates example databases. Then check the default configuration [file](./src/test/resources/default.properties). If you need to override anything, create `application.properties` file in the same directory with new values.