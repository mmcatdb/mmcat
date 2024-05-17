# server

A backend application for the MM-cat tool. It is based on the Spring Boot framework.

## Configuration

- Copy the sample configuration file to `application.properties` and fill all the necessary information.
```bash
cp src/main/resources/default.properties ./application.properties
```
- *Note: It's important to put the file in this directory. Otherwise, it would be bundled in the `.jar` file, so its secrets would be exposed. Also, now it's possible to change the values even after compilation.*
- Additional settings are in the `src/main/java/cz/matfyz/server/Settings.java` file.
- For more information see the [Configuration Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config).

## Installation

- The application is supposed to cooperate with database, example databases and a frontend application. Therefore, it should be run in via docker. For development purposes, you can use the following commands.

### Run development server

```bash
mvn spring-boot:run
```
- Whenever something in this directory changes, the server will be automatically reloaded.
    - However, changes in other modules won't be reflected. You need to recompile the whole project, see the [Library](../README.md) documentation.

### Build application

- The application is already compiled and packed in the `.jar` file. You can run it by:
```bash
java -jar target/server-1.0-SNAPSHOT-app.jar
```
