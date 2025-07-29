#!/bin/bash

# cypher-shell -f createExample.cypher -a bolt://localhost:7687 -u ${EXAMPLE_USERNAME} -p ${EXAMPLE_PASSWORD}

cypher-shell -f setupNeo4j.cypher -u ${NEO4J_USERNAME} -p ${EXAMPLE_PASSWORD}
cypher-shell -f setupNeo4jAdminer.cypher -u ${NEO4J_USERNAME} -p ${EXAMPLE_PASSWORD}
