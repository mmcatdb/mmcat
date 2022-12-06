# server

A backend application for the MM-evocat tool. It is based on the Spring Boot framework.

## Requirements

- Java 17 (JDK)
- Apache Maven 3.8
- PostgreSQL 12.10
- MongoDB 3.2 (for the example only)

## Configuration

- Copy the sample configuration file and fill all the necessary information.
```sh
cp src/main/resources/application.properties.sample src/main/resources/application.properties
```
- Additional settings are in the `src/main/java/cz/cuni/matfyz/server/Settings.java` file.
- For more information see the [Configuration Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config).

## Installation

### Setup

The application needs one main database (to store its own data, must be PostgreSQL) as well as some other databases (from which it can import data to the instance category).
- First create a user and the main database. To do so, open the PostgreSQL console:
```sh
sudo -u postgres psql
```
- and write:
```sql
CREATE ROLE mmcat_user LOGIN PASSWORD 'mmcat_password';
CREATE DATABASE mmcat_server OWNER mmcat_user;
```
- Feel free to use different credentials and database name (just make sure you replace them in the rest of this document).
- Then create the database structure from the sample script:
```sh
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server?sslmode=require -f src/main/resources/createDatabase.sql
```

### Run application in place

```sh
mvn spring-boot:run
```
- To run the application independently execute the command in some detachable environment (Screen, Tmux etc.).

## Sample data

- The example database assumes there are two databases for the data import, one PostgreSQL and the other MongoDB.
- The first one can be created by:
```sql
CREATE DATABASE mmcat_server_data OWNER mmcat_user;
```
- and data can be loaded by:
```sh
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_data?sslmode=require -f src/main/resources/setupPostgresql.sql
```
- You also need to create a user for the second one. Open the MongoDB console and write:
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
```sh
mongo --username mmcat_user --password mmcat_password --authenticationDatabase admin localhost:27017/mmcat_server_data src/main/resources/setupMongodb.js
```

## Other data
```sql
CREATE DATABASE mmcat_server_ttd OWNER mmcat_user;
```

```sh
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_ttd?sslmode=require -f src/main/resources/setupPostgresqlTTD.sql
```
