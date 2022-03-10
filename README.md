# Server
## Setup PostgreSQL database
- Open console as postgres (i.e. root) to create new databases:
```console
sudo -u postgres psql
```
- Create database:
```sql
CREATE DATABASE mmcat_server OWNER mmcat_user;
```
- Run SQL script:
```console
psql -U mmcat_user -h localhost -f src/main/resources/createDatabase.sql mmcat_server
```

## Run
- Required Maven ^3.8.4.
- In the root directory: (must be done after any change in the dependent modules)
```console
mvn install
```

- In the `server/` directory:
```console
mvn spring-boot:run
```

## Setup database
- From the `thesis/` directory.
```console
sqlite3 data/test.db < server/src/main/resources/createDatabase.sql
```

# Databases
## MongoDB
- Create user for the database (parameters are specified in `transformations/src/test/resources/config.properties`):
```js
use <mongodb.database>
db.createUser({
    user: "<mongodb.username>",
    pwd: "<mongodb.password>",
    roles: [ { role: "readWrite", db: "<mongodb.database>" } ]
});
```

## PostgreSQL
- Open console as user:
```console
psql -U mmcat_user -h localhost mmcat_test
```
