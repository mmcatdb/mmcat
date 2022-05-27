# Server
## Setup PostgreSQL database
- Open console as postgres (i.e. root) to create new databases:
```console
sudo -u postgres psql
```
- Create role (user):
```sql
CREATE ROLE mmcat_user LOGIN PASSWORD 'mmcat_password';
```
- Create database:
```sql
CREATE DATABASE mmcat_server OWNER mmcat_user;
```
- Run SQL script:
```console
psql -U mmcat_user -h localhost -f src/main/resources/createDatabase.sql mmcat_server
```
- Better way (no need to write password):
```
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server?sslmode=require -f src/main/resources/createDatabase.sql
```

## Run
- Required Maven ^3.8.4.
- In the root directory: (must be done after any change in the dependent modules)
```console
mvn install -Dmaven.test.skip
```

- In the `server/` directory:
```console
mvn spring-boot:run
```

# Databases
## MongoDB
- Create user for the database (parameters are specified in `transformations/src/test/resources/config.properties`):
```js
use admin;
db.createUser({
    user: "<mongodb.username>",
    pwd: "<mongodb.password>",
    roles: [ { role: "readWrite", db: "<mongodb.database>" } ]
});
```
- The `admin` means that the user will be authenticated agains the `admin` database. That's ok - the user won't have any privileges to the `admin` database.
- Full commands:
```js
use admin;
db.createUser({
    user: "mmcat_user",
    pwd: "mmcat_password",
    roles: [
        { role: "readWrite", db: "mmcat_test" },
        { role: "readWrite", db: "mmcat_server_data" },
    ]
});
```

### Execute file
```console
mongo --username mmcat_user --password mmcat_password --authenticationDatabase admin localhost:27017/mmcat_server_data src/main/resources/setupMongodb.js
```

## PostgreSQL
- Open console as user:
```console
psql -U mmcat_user -h localhost mmcat_server_data
```

# Complete server setup
- TODO
```¨console
cd example-ui
npm run build
cd ..
mvn install -Dmaven.test.skip
cd server
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server?sslmode=require -f src/main/resources/createDatabase.sql
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_data?sslmode=require -f src/main/resources/setupPostgresql.sql
mongo --username mmcat_user --password mmcat_password --authenticationDatabase admin localhost:27017/mmcat_server_data src/main/resources/setupMongodb.js
```

# Backlog
