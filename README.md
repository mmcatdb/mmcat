# Server
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