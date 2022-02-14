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
