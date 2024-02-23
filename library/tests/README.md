# tests

Because all parts of the framework depend on the same basic concepts (schema category, transformation algorithms, etc.) most of their tests are here. The main code defines commonly used test data and structures while the test code contains the tests themselves.

The tests for the server package are not included here - on the contrary, the server package depends on this one.

## Configuration

First, you need the test databases. Those should be created by the same docker command that creates example databases. Then check the default configuration [file](./src/test/resources/default.properties). If you need to override anything, create `application.properties` file in the same directory with new values.

## Setup scripts

In order to run tests on databases, we have to setup them first. This means running setup scripts. However, for some unknown reason, some of the database drivers' providers don't allow to run arbitrary scripts. Others allow string commands, but only one statement at a time - meaning that we have to parse the scripts ourselves.

For this reason, the application relies on several programs to run the setup scripts. Make sure these are available and executable on your system:
- `mongosh` (MongoDB Shell)

In other database systems, format your scripts very strictly. This means:
- Each statement or comment on a separate line.
- No multiline comments.
