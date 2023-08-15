# server

A backend application for the MM-cat tool. It is based on the Spring Boot framework.

## Configuration

- Copy the sample configuration file and fill all the necessary information.
```bash
cp src/main/resources/default.properties src/main/resources/application.properties
```
- Additional settings are in the `src/main/java/cz/matfyz/server/Settings.java` file.
- For more information see the [Configuration Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config).

## Installation

- The application is supposed to cooperate with database, example databases and a frontend application. Therefore, it should be run in via docker. For development purposes, you can use the following commands.

### Run development server

```bash
mvn spring-boot:run
```

### Build application

```bash
mvn clean package
```

- You can then run the application by:

```bash
java -jar target/server-1.0-SNAPSHOT.jar
```
