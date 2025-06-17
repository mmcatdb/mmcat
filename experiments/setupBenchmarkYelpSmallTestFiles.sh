#!/bin/sh

rootdir="$(dirname "$0")/.."
datadir="$rootdir/data"

source "$rootdir/.env"

for name in business user review; do
    head "$datadir/Yelp JSON/yelp_academic_dataset_${name}.json" > "$datadir/Yelp JSON/yelp_academic_dataset_${name}_small.json"
done
