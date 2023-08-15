#!/bin/bash

# cypher-shell -f createExample.cypher -a bolt://localhost:7687 -u ${EXAMPLE_USERNAME} -p ${EXAMPLE_PASSWORD}

cypher-shell -f createExample.cypher -u neo4j -p ${EXAMPLE_PASSWORD}
