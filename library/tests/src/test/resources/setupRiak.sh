#!/bin/bash

# Delete index

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "props" : { "search_index" : "_dont_index_" } }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/props

curl -s -X DELETE http://${HOST}:${HTTP_PORT}/search/index/${1}_iactors

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "props" : { "search_index" : "_dont_index_" } }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_movies/props

curl -s -X DELETE http://${HOST}:${HTTP_PORT}/search/index/${1}_imovies

# Delete buckets

curl -s -X GET http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/keys?keys=true | jq -r '.keys[]' | while read -r key
do
    curl -s -X DELETE http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/keys/${key}
done

curl -s -X GET http://${HOST}:${HTTP_PORT}/buckets/${1}_movies/keys?keys=true | jq -r '.keys[]' | while read -r key
do
    curl -s -X DELETE http://${HOST}:${HTTP_PORT}/buckets/${1}_movies/keys/${key}
done

# Index

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "schema" : "_yz_default" }' \
    http://${HOST}:${HTTP_PORT}/search/index/${1}_iactors

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d "{ \"props\" : { \"search_index\" : \"${1}_iactors\" } }" \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/props

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d '{ "schema" : "_yz_default" }' \
    http://${HOST}:${HTTP_PORT}/search/index/${1}_imovies

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -d "{ \"props\" : { \"search_index\" : \"${1}_imovies\" } }" \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_movies/props

# Actors

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </buckets/${1}_movies/keys/samotari>; riaktag=\"tmovie\"" \
    -H "Link: </buckets/${1}_movies/keys/medvidek>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Ivan Trojan", "year_i" : 1964 }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/keys/trojan

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </buckets/${1}_movies/keys/vratnelahve>; riaktag=\"tmovie\"" \
    -H "Link: </buckets/${1}_movies/keys/samotari>; riaktag=\"tmovie\"" \
    -H "Link: </buckets/${1}_movies/keys/medvidek>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Jiří Macháček", "year_i" : 1966 }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/keys/machacek

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </buckets/${1}_movies/keys/samotari>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Jitka Schneiderová", "year_i" : 1973 }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/keys/schneiderova

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </buckets/${1}_movies/keys/vratnelahve>; riaktag=\"tmovie\"" \
    -d '{ "name_s" : "Zdeněk Svěrák", "year_i" : 1936 }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_actors/keys/sverak

# Movies

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </buckets/${1}_actors/keys/sverak>; riaktag=\"tactor\"" \
    -H "Link: </buckets/${1}_actors/keys/machacek>; riaktag=\"tactor\"" \
    -d '{ "title_s" : "Vratné lahve", "year_i" : 2006, "actors_ss" : [ "Zdeněk Svěrák", "Jiří Macháček" ] }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_movies/keys/vratnelahve

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </buckets/${1}_actors/keys/schneiderova>; riaktag=\"tactor\"" \
    -H "Link: </buckets/${1}_actors/keys/trojan>; riaktag=\"tactor\"" \
    -H "Link: </buckets/${1}_actors/keys/machacek>; riaktag=\"tactor\"" \
    -d '{ "title_s" : "Samotáři", "year_i" : 2000, "actors_ss" : [ "Jitka Schneiderová", "Ivan Trojan", "Jiří Macháček" ] }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_movies/keys/samotari

curl -s -X PUT \
    -H 'Content-Type: application/json' \
    -H "Link: </buckets/${1}_actors/keys/machacek>; riaktag=\"tactor\"" \
    -H "Link: </buckets/${1}_actors/keys/trojan>; riaktag=\"tactor\"" \
    -d '{ "title_s" : "Medvídek", "year_i" : 2007, "actors_ss" : [ "Jiří Macháček", "Ivan Trojan" ] }' \
    http://${HOST}:${HTTP_PORT}/buckets/${1}_movies/keys/medvidek
