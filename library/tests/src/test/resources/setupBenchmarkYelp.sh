#!/bin/sh

# NOTE: This requires package mongo-tools (or mongodb-tools) to work.

docker_mongodb_port="3205"
docker_mongodb_instance="localhost:${docker_mongodb_port}"

# library/tests/src/test/resources
rootdir="$(dirname "$0")/../../../../.."
datadir="$rootdir/data"

source "$rootdir/.env"

mongosh --port $docker_mongodb_port << EOSQL
    use admin;
    db.auth("${EXAMPLE_USERNAME}", "${EXAMPLE_PASSWORD}");
    use ${BENCHMARK_DATABASE_YELP};

    if (
        db.business.countDocuments() == 150346 &&
        db.user.countDocuments() == 1987897 &&
        db.review.countDocuments() == 6990280
    ) {
        exit(42);
    } else {
        console.log(db.business.countDocuments());
        console.log(db.user.countDocuments());
        console.log(db.review.countDocuments());
        db.business.drop()
        db.user.drop()
        db.review.drop()
        exit(48);
    }
EOSQL

retval=$?

if [ $retval -eq 42 ]; then
    echo "Yelp (probably) found in MongoDB - skipping import; done."
    exit 0
elif [ $retval -ne 48 ]; then
    echo "Something has gone wrong while checking MongoDB; skipping."
    exit 1
fi

mkdir -p "$datadir"

## Yelp forbids from downloading the file using wget, i think, so it has to be done manually
if [ -f "$datadir/Yelp-JSON.zip" ]; then
    # echo "Yelp archive found in data/ - skipping download."
    echo "Yelp archive found in data/ - OK."
else
    # echo "Downloading..."
    # wget https://business.yelp.com/external-assets/files/Yelp-JSON.zip -P "$datadir"

    echo "Yelp dataset not found."
    echo "Download the dataset from https://business.yelp.com/data/resources/open-dataset/"
    echo "Afterwards, insert it into the data/ directory."
    exit 1
fi

if  [ -f "$datadir/Yelp JSON/yelp_academic_dataset_business.json" ] &&
    [ -f "$datadir/Yelp JSON/yelp_academic_dataset_user.json" ] &&
    [ -f "$datadir/Yelp JSON/yelp_academic_dataset_review.json" ]
then
    echo "Extracted Yelp data found - skipping unpacking."
else
    echo "Unpacking..."
    export UNZIP_DISABLE_ZIPBOMB_DETECTION=TRUE
    unzip "$datadir/Yelp-JSON.zip" -d "$datadir"
    tar --directory "$datadir/Yelp JSON/" -xzf "$datadir/Yelp JSON/yelp_dataset.tar"
    export UNZIP_DISABLE_ZIPBOMB_DETECTION=
fi

# WARNING: Importing takes a long time, at least 2 minutes

echo "Importing into MongoDB..."
mongoimport --uri mongodb://${EXAMPLE_USERNAME}:${EXAMPLE_PASSWORD}@${docker_mongodb_instance}/${BENCHMARK_DATABASE_YELP}?authSource=admin \
    --collection business --type json --file "$datadir/Yelp JSON/yelp_academic_dataset_business.json"

mongoimport --uri mongodb://${EXAMPLE_USERNAME}:${EXAMPLE_PASSWORD}@${docker_mongodb_instance}/${BENCHMARK_DATABASE_YELP}?authSource=admin \
    --collection user --type json --file "$datadir/Yelp JSON/yelp_academic_dataset_user.json"

mongoimport --uri mongodb://${EXAMPLE_USERNAME}:${EXAMPLE_PASSWORD}@${docker_mongodb_instance}/${BENCHMARK_DATABASE_YELP}?authSource=admin \
    --collection review --type json --file "$datadir/Yelp JSON/yelp_academic_dataset_review.json"

echo "Done."
