#!/bin/bash

# cypher-shell -f example_basic.cypher -a bolt://localhost:7687 -u ${DB_EXAMPLE_USERNAME} -p ${DB_EXAMPLE_PASSWORD}

# Enable job monitor.
set -m

# Start the primary process and put it in the background.
/startup/docker-entrypoint.sh $1 &

# Wait for Neo4j.
wget --quiet --tries=10 --waitretry=2 -O /dev/null http://localhost:7474

./entrypoint.sh

# Return the primary process back to foreground.
fg %1
