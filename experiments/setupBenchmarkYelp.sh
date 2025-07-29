#!/bin/sh

# NOTE: This requires package mongo-tools (or mongodb-tools) and cypher-shell to work.

docker_postgresql_port=""

# library/tests/src/test/resources
rootdir="$(realpath $(dirname "$0")/..)"
datadir="$rootdir/data"

source "$rootdir/.env"
mkdir -p "$datadir"


if [ $1 = "help" ] || [ $1 = "--help" ]; then
    echo "Usage:"
    echo "  ./setupBenchmarkYelp.sh [small]"
    echo "          Setups all databases."
    echo "  ./setupBenchmarkYelp.sh <DB> [small]"
    echo "          Setups a single database (<DB> may be: postgresql | mongodb | neo4j)."
    echo ""
    echo "\"small\" is an optional parameter to setup with only small test subset of data (<= 20 rows)."
    exit
fi

# YELP FILES (extracting)

if [ ${1:-"large"} = "small" ] || [ ${2:-"large"} = "small" ]; then
    echo "Using small dataset..."
    businessFile="$rootdir/experiments/benchmarkDataSetup/yelp_business_small.json"
    userFile="$rootdir/experiments/benchmarkDataSetup/yelp_user_small.json"
    reviewFile="$rootdir/experiments/benchmarkDataSetup/yelp_review_small.json"
else
    businessFile="$datadir/Yelp JSON/yelp_academic_dataset_business.json"
    userFile="$datadir/Yelp JSON/yelp_academic_dataset_user.json"
    reviewFile="$datadir/Yelp JSON/yelp_academic_dataset_review.json"

    if  [ -f "$datadir/Yelp JSON/yelp_academic_dataset_business.json" ] &&
        [ -f "$datadir/Yelp JSON/yelp_academic_dataset_user.json" ] &&
        [ -f "$datadir/Yelp JSON/yelp_academic_dataset_review.json" ]
    then
        echo "Extracted Yelp data found; proceeding with import..."
    else
        if [ -f "$datadir/Yelp-JSON.zip" ]; then
            echo "Yelp archive found in data; extracting..."
        else
            echo "Yelp dataset not found. See experiments/README.md#query-benchmarks"
            exit 1
        fi
        export UNZIP_DISABLE_ZIPBOMB_DETECTION=TRUE
        unzip "$datadir/Yelp-JSON.zip" -d "$datadir"
        tar --directory "$datadir/Yelp JSON/" -xzf "$datadir/Yelp JSON/yelp_dataset.tar"
        export UNZIP_DISABLE_ZIPBOMB_DETECTION=
    fi
fi

if [ $# = 0 ] || [ $# = 1 -a $1 = "small" ] || [ $1 = "mongodb" ]; then
    echo "Importing into MongoDB..."

    mongosh --port 3205 << EOSQL
        use admin;
        db.auth("${EXAMPLE_USERNAME}", "${EXAMPLE_PASSWORD}");
        use ${BENCHMARK_DATABASE_YELP};

        db.business.drop()
        db.user.drop()
        db.review.drop()
EOSQL

    mongoimport --uri mongodb://${EXAMPLE_USERNAME}:${EXAMPLE_PASSWORD}@localhost:3205/${BENCHMARK_DATABASE_YELP}?authSource=admin \
        --collection business --type json --file "${businessFile}"
    mongoimport --uri mongodb://${EXAMPLE_USERNAME}:${EXAMPLE_PASSWORD}@localhost:3205/${BENCHMARK_DATABASE_YELP}?authSource=admin \
        --collection user --type json --file "${userFile}"
    mongoimport --uri mongodb://${EXAMPLE_USERNAME}:${EXAMPLE_PASSWORD}@localhost:3205/${BENCHMARK_DATABASE_YELP}?authSource=admin \
        --collection review --type json --file "${reviewFile}"
fi

if [ $# = 0 ] || [ $# = 1 -a $1 = "small" ] || [ $1 = "postgresql" ]; then
    echo "Importing into PostgreSQL..."

    export PGPASSWORD="${EXAMPLE_PASSWORD}"
    node benchmarkDataSetup/postgreSQL.js "${businessFile}" "${userFile}" "${reviewFile}" \
    | psql -h localhost -p 3204 -d ${BENCHMARK_DATABASE_YELP} -U ${EXAMPLE_USERNAME}
fi

if [ $# = 0 ] || [ $# = 1 -a $1 = "small" ] || [ $1 = "neo4j" ]; then
    echo "Importing into Neo4j..."

    export NEO4J_PASSWORD="${EXAMPLE_PASSWORD}"
    node benchmarkDataSetup/neo4j.js "${businessFile}" "${userFile}" "${reviewFile}" \
    | cypher-shell -a neo4j://localhost:3206 -u "${NEO4J_USERNAME}"
fi

echo "Done."
