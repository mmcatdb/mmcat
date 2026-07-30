#!/bin/bash

url="${1}"
type="${2}"

# Delete index

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "props" : { "search_index" : "_dont_index_" } }' \
    "${url}/types/${type}/buckets/actors/props"

curl -s -X DELETE "${url}/search/index/${type}_actors"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "props" : { "search_index" : "_dont_index_" } }' \
    "${url}/types/${type}/buckets/movies/props"

curl -s -X DELETE "${url}/search/index/${type}_movies"

# Delete buckets

curl -s -X GET "${url}/types/${type}/buckets/actors/keys?keys=true" | jq -r '.keys[]' | while read -r key
do
    curl -s -X DELETE "${url}/types/${type}/buckets/actors/keys/${key}"
done

curl -s -X GET "${url}/types/${type}/buckets/movies/keys?keys=true" | jq -r '.keys[]' | while read -r key
do
    curl -s -X DELETE "${url}/types/${type}/buckets/movies/keys/${key}"
done

# Index

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "schema" : "_yz_default" }' \
    "${url}/search/index/${type}_actors"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d "{ \"props\" : { \"search_index\" : \"${type}_actors\" } }" \
    "${url}/types/${type}/buckets/actors/props"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "schema" : "_yz_default" }' \
    "${url}/search/index/${type}_movies"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d "{ \"props\" : { \"search_index\" : \"${type}_movies\" } }" \
    "${url}/types/${type}/buckets/movies/props"

# Actors

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </types/${type}/buckets/movies/keys/samotari>; riaktag=\"tmovie\"" \
    -H "Link: </types/${type}/buckets/movies/keys/medvidek>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Ivan Trojan", "year_i" : 1964 }' \
    "${url}/types/${type}/buckets/actors/keys/trojan"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </types/${type}/buckets/movies/keys/vratnelahve>; riaktag=\"tmovie\"" \
    -H "Link: </types/${type}/buckets/movies/keys/samotari>; riaktag=\"tmovie\"" \
    -H "Link: </types/${type}/buckets/movies/keys/medvidek>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Jiří Macháček", "year_i" : 1966 }' \
    "${url}/types/${type}/buckets/actors/keys/machacek"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </types/${type}/buckets/movies/keys/samotari>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Jitka Schneiderová", "year_i" : 1973 }' \
    "${url}/types/${type}/buckets/actors/keys/schneiderova"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </types/${type}/buckets/movies/keys/vratnelahve>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Zdeněk Svěrák", "year_i" : 1936 }' \
    "${url}/types/${type}/buckets/actors/keys/sverak"

# Movies

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </types/${type}/buckets/actors/keys/sverak>; riaktag=\"tactor\"" \
    -H "Link: </types/${type}/buckets/actors/keys/machacek>; riaktag=\"tactor\"" \
    -d '{ "title_s" : "Vratné lahve", "year_i" : 2006, "actors_ss" : [ "Zdeněk Svěrák", "Jiří Macháček" ] }' \
    "${url}/types/${type}/buckets/movies/keys/vratnelahve"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </types/${type}/buckets/actors/keys/schneiderova>; riaktag=\"tactor\"" \
    -H "Link: </types/${type}/buckets/actors/keys/trojan>; riaktag=\"tactor\"" \
    -H "Link: </types/${type}/buckets/actors/keys/machacek>; riaktag=\"tactor\"" \
    -d '{ "title_s" : "Samotáři", "year_i" : 2000, "actors_ss" : [ "Jitka Schneiderová", "Ivan Trojan", "Jiří Macháček" ] }' \
    "${url}/types/${type}/buckets/movies/keys/samotari"

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </types/${type}/buckets/actors/keys/machacek>; riaktag=\"tactor\"" \
    -H "Link: </types/${type}/buckets/actors/keys/trojan>; riaktag=\"tactor\"" \
    -d '{ "title_s" : "Medvídek", "year_i" : 2007, "actors_ss" : [ "Jiří Macháček", "Ivan Trojan" ] }' \
    "${url}/types/${type}/buckets/movies/keys/medvidek"
