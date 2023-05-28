#!/bin/bash

# mongo --username ${DB_EXAMPLE_USERNAME} --password ${DB_EXAMPLE_PASSWORD} --authenticationDatabase admin localhost:27017/example_basic src/main/resources/example_basic.js

mongosh << EOSQL
use admin;
db.createUser({
    user: "${DB_EXAMPLE_USERNAME}",
    pwd: "${DB_EXAMPLE_PASSWORD}",
    roles: [
        { role: "readWrite", db: "example_basic" },
        { role: "readWrite", db: "example_query" },
    ],
});
db.auth("${DB_EXAMPLE_USERNAME}", "${DB_EXAMPLE_PASSWORD}");
use example_basic;
load('example_basic.js');

use example_query;
load('example_query.js');
EOSQL
