# server

A backend application for the MM-evocat tool. It is based on the Spring Boot framework.

## Requirements

- Java 17 (JDK)
- Apache Maven 3.8
- PostgreSQL 12.10
- MongoDB 3.2 (for the example only)

## Configuration

- Copy the sample configuration file and fill all the necessary information.
```bash
cp src/main/resources/application.properties.sample src/main/resources/application.properties
```
- Additional settings are in the `src/main/java/cz/cuni/matfyz/server/Settings.java` file.
- For more information see the [Configuration Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config).

## Installation

### Setup

The application needs one main database (to store its own data, must be PostgreSQL) as well as some other databases (from which it can import data to the instance category).
- First create a user and the main database. To do so, open the PostgreSQL console:
```bash
sudo -u postgres psql
```
- and write:
```sql
CREATE ROLE mmcat_user LOGIN PASSWORD 'mmcat_password';
CREATE DATABASE mmcat_server OWNER mmcat_user;
```
- Feel free to use different credentials and database name (just make sure you replace them in the rest of this document).
- Then create the database structure from the sample script:
```bash
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server?sslmode=require -f src/main/resources/createDatabase.sql
```

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

## Sample data

### PostgreSQL

- The database can be created by:
```sql
CREATE DATABASE mmcat_server_data OWNER mmcat_user;
```
- and data can be loaded by:
```bash
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_data?sslmode=require -f src/main/resources/setupPostgresql.sql
```

### MongoDB
- You need to create a user for this one. Open the MongoDB console and write:
```js
use admin;
db.createUser({
    user: "mmcat_user",
    pwd: "mmcat_password",
    roles: [
        { role: "readWrite", db: "mmcat_server_data" },
        //{ role: "readWrite", db: "mmcat_server_experiments" },
    ]
});
```
- Then you can create the database:
```bash
cypher-shell -f src/main/resources/setupNeo4j.cypher -a bolt://localhost:7687 -u mmcat_user -p mmcat_password
```

### Neo4j
```bash

```

## Other data
```sql
CREATE DATABASE mmcat_server_ttd OWNER mmcat_user;
```

```bash
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_ttd?sslmode=require -f src/main/resources/setupPostgresqlTTD.sql
```


# TODO


-- CREATE DATABASE mmcat_database;

-- CREATE USER mmcat_user SET PLAINTEXT PASSWORD 'mmcat_password' SET PASSWORD CHANGE NOT REQUIRED SET HOME DATABASE mmcat_database
CREATE USER mmcat_user SET PLAINTEXT PASSWORD 'mmcat_password' SET PASSWORD CHANGE NOT REQUIRED;