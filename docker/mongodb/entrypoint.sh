#!/bin/bash

# mongo --username ${EXAMPLE_USERNAME} --password ${EXAMPLE_PASSWORD} --authenticationDatabase admin localhost:27017/example src/main/resources/setupMongodb.js

mongosh << EOSQL
use admin;
db.createUser({
    user: "${EXAMPLE_USERNAME}",
    pwd: "${EXAMPLE_PASSWORD}",
    roles: [
        { role: "readWrite", db: "${EXAMPLE_DATABASE}" },
        { role: "readWrite", db: "test" },
    ],
});
db.auth("${EXAMPLE_USERNAME}", "${EXAMPLE_PASSWORD}");
use ${EXAMPLE_DATABASE};
load('setupMongodb.js');
EOSQL
