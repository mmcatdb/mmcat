#!/bin/bash

echo -e "\n\
server.port=${SERVER_PORT}\n\
server.origin=${EXAMPLE_UI_DOMAIN}\n\
\n\
postgresql.username=${DB_USERNAME}\n\
postgresql.password=${DB_PASSWORD}\n\
postgresql.password=${DB_DATABASE}" >> server/src/main/resources/application.properties

# tail -f /dev/null
java -jar server/target/server-1.0-SNAPSHOT.jar