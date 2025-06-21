#!/bin/bash

# mongo --username ${EXAMPLE_USERNAME} --password ${EXAMPLE_PASSWORD} --authenticationDatabase admin localhost:27017/${EXAMPLE_DATABASE_BASIC} src/main/resources/setupMongodbBasic.js

mongosh << EOSQL
use admin;
db.createUser({
    user: "${EXAMPLE_USERNAME}",
    pwd: "${EXAMPLE_PASSWORD}",
    roles: [
        { role: "readWrite", db: "${EXAMPLE_DATABASE_BASIC}" },
        { role: "readWrite", db: "${EXAMPLE_DATABASE_ADMINER}" },
        { role: "readWrite", db: "${EXAMPLE_DATABASE_QUERY_EVOLUTION}" },
        { role: "readWrite", db: "${EXAMPLE_DATABASE_INFERENCE}" },
        { role: "readWrite", db: "${BENCHMARK_DATABASE_YELP}" },
        { role: "readWrite", db: "test" },
    ],
});
db.auth("${EXAMPLE_USERNAME}", "${EXAMPLE_PASSWORD}");
use ${EXAMPLE_DATABASE_BASIC};
load('setupMongodbBasic.js');
use ${EXAMPLE_DATABASE_ADMINER};
load('setupMongodbAdminer.js');
use ${EXAMPLE_DATABASE_QUERY_EVOLUTION};
load('setupMongodbQueryEvolution.js');
use ${EXAMPLE_DATABASE_INFERENCE};
load('setupMongodbInference.js');
EOSQL
