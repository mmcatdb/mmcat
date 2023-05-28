#!/bin/bash

cypher-shell -f example_basic.cypher -u neo4j -p ${DB_EXAMPLE_PASSWORD}
cypher-shell -f example_query.cypher -u neo4j -p ${DB_EXAMPLE_PASSWORD}
