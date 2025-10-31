## PostgreSQL

- create new db
```sql
CREATE DATABASE mmcat_server_experiments OWNER mmcat_user;
```
- load data
```sh
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_experiments?sslmode=require -f createPostgresql.sql
psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_experiments?sslmode=require -f setupPostgresql.sql
```

## MongoDB

- create new db (add role to user)
```js
db.grantRolesToUser(
    "mmcat_user",
    [
        { role: "readWrite", db: "mmcat_server_experiments" }
    ]
)
```
- load data
```sh
mongo --username mmcat_user --password mmcat_password --authenticationDatabase admin localhost:27017/mmcat_server_experiments setupMongodb.js
mongo --username mmcat_user --password mmcat_password --authenticationDatabase admin localhost:27017/mmcat_server_experiments initialUserData.js
```

## Query Benchmarks

Some query tests use the *Yelp* dataset to test performance on large data, but due to its size, it is not loaded automatically. To load the dataset, download it from <https://business.yelp.com/data/resources/open-dataset/>, then insert either the archive or extracted data (i.e. the `yelp_academic_dataset_*.json` files) into the `data/` directory. Finally, run the `setupBenchmarkYelp.sh` script to load the data into the database. *Warning:* loading may take a long time.
