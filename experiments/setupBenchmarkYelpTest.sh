#!/bin/sh

# NOTE: This requires package mongo-tools (or mongodb-tools) to work.

docker_mongodb_port="3205"
docker_mongodb_instance="localhost:${docker_mongodb_port}"

docker_postgresql_port="3204"

# library/tests/src/test/resources
rootdir="$(dirname "$0")/.."
datadir="$rootdir/data"

source "$rootdir/.env"


export PGPASSWORD="${EXAMPLE_PASSWORD}"

echo "Importing into PostgreSQL..."

node setupBenchmarkYelpPostgreSQL.js \
    "$datadir/Yelp JSON/yelp_academic_dataset_business_small.json" \
    "$datadir/Yelp JSON/yelp_academic_dataset_user_small.json" \
    "$datadir/Yelp JSON/yelp_academic_dataset_review_small.json" \
| psql -h localhost -p ${docker_postgresql_port} -d ${BENCHMARK_DATABASE_YELP} -U ${EXAMPLE_USERNAME}

echo "Done."
