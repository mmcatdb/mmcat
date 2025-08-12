#!/bin/sh

# NOTE: This requires package mongo-tools (or mongodb-tools) and cypher-shell to work.

if [ $1 = "help" ] || [ $1 = "--help" ]; then
    echo "Usage:"
    echo "  ./setupBenchmarkYelp.sh [small]"
    echo "          Setups all databases."
    echo "  ./setupBenchmarkYelp.sh <DB> [small]"
    echo "          Setups a single database (<DB> may be: postgresql | mongodb | neo4j)."
    echo ""
    echo "\"small\" is an optional parameter to setup with only small test subset of data (<= 20 rows)."
    echo "Note that the installation requires packages mongo-tools (or mongodb-tools) and cypher-shell to work."
    exit
fi

docker_postgresql_port=""

# library/tests/src/test/resources
rootdir="$(realpath $(dirname "$0")/..)"
datadir="$rootdir/data"

source "$rootdir/.env"
mkdir -p "$datadir"


# YELP FILES (extracting)

if [ ${1:-"large"} = "small" ] || [ ${2:-"large"} = "small" ]; then
    echo "Using small dataset..."
    cp "$rootdir/experiments/benchmarkDataSetup/yelp_*_small.json" "$datadir"
    businessFileBase="yelp_business_small.json"
    userFileBase="yelp_user_small.json"
    reviewFileBase="yelp_review_small.json"

    businessFile="$datadir/$businessFileBase"
    userFile="$datadir/$userFileBase"
    reviewFile="$datadir/$reviewFileBase"
else
    businessFileBase="yelp_academic_dataset_business.json"
    userFileBase="yelp_academic_dataset_user.json"
    reviewFileBase="yelp_academic_dataset_review.json"

    businessFile="$datadir/$businessFileBase"
    userFile="$datadir/$userFileBase"
    reviewFile="$datadir/$reviewFileBase"

    if  [ -f "$businessFile" ] && [ -f "$userFile" ] && [ -f "$reviewFile" ]; then
        echo "Extracted Yelp data found; proceeding with import..."
    elif [ -f "$datadir/Yelp-JSON.zip" ]; then
        echo "Yelp archive found in data; extracting..."
        export UNZIP_DISABLE_ZIPBOMB_DETECTION=TRUE
        unzip "$datadir/Yelp-JSON.zip" -d "$datadir"
        tar --directory "$datadir/" -xzf "$datadir/Yelp JSON/yelp_dataset.tar"
        export UNZIP_DISABLE_ZIPBOMB_DETECTION=
    else
        echo "Yelp dataset not found. See experiments/README.md#query-benchmarks"
        exit 1
    fi
fi

if [ $# = 0 ] || [ $# = 1 -a $1 = "small" ] || [ $1 = "mongodb" ]; then
    echo "Importing into MongoDB..."

    mongosh --port 3205 << EOJS
        use admin;
        db.auth("${EXAMPLE_USERNAME}", "${EXAMPLE_PASSWORD}");
        use ${BENCHMARK_DATABASE_YELP};

        db.business.drop();
        db.user.drop();
        db.review.drop();
EOJS

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
    | cypher-shell -a neo4j://localhost:3206 -u neo4j

    cypher-shell -a neo4j://localhost:3206 -u neo4j << EOCYPHER
        MATCH (a:YelpBusiness)-[r]->() DELETE a, r;
        MATCH (a:YelpUser)-[r]->() DELETE a, r;
        MATCH (a:YelpBusiness) DELETE a;
        MATCH (a:YelpUser) DELETE a;

        CALL apoc.load.json(${businessFileBase}) YIELD value
        CALL {
            WITH value
            CREATE (:YelpBusiness { business_id: value.business_id, name: value.name, city: value.city, state: value.state, stars: value.stars, review_count: value.review_count, is_open: value.is_open })
        } IN TRANSACTIONS OF 10000 ROWS;

        CREATE INDEX yelp_business_id FOR (n:YelpBusiness) ON (n.business_id);

        CALL apoc.load.json(${userFileBase}) YIELD value
        CALL {
            WITH value
            CREATE (:YelpUser { user_id: value.user_id, name: value.name, review_count: value.review_count, yelping_since: value.yelping_since, useful: value.useful, funny: value.funny, cool: value.cool })
        } IN TRANSACTIONS OF 10000 ROWS;

        CREATE INDEX yelp_user_id FOR (n:YelpUser) ON (n.user_id);

        CALL apoc.load.json(${userFileBase}) YIELD value
        CALL {
            WITH value
            UNWIND split(value.friends, ', ') AS friend_id
            MATCH (a:YelpUser {user_id: value.user_id}), (b:YelpUser {user_id: friend_id})
            CREATE (a)-[:YELP_FRIENDSHIP]->(b)
        } IN TRANSACTIONS OF 10000 ROWS;

        CALL apoc.load.json(${reviewFileBase}) YIELD value
        CALL {
            WITH value
            MATCH (a:YelpUser {user_id: value.user_id}), (b:YelpBusiness {business_id: value.business_id})
            CREATE (a)-[:YELP_REVIEW { review_id: value.review_id, stars: value.stars, date: value.date, useful: value.useful, funny: value.funny, cool: value.cool }]->(b)
        } IN TRANSACTIONS OF 10000 ROWS;

        CREATE INDEX yelp_review_id FOR ()-[r:YELP_REVIEW]-() ON (r.review_id);

EOCYPHER

fi

echo "Done."
