#!/bin/bash

# psql "postgresql://${DATABASE_USERNAME}:${DATABASE_PASSWORD}@localhost:${DATABASE_PORT}/${DATABASE_DATABASE}" -f src/main/resources/createDatabase.sql

# TODO this should be mostly achieved by some app command, like create:databases or similar

psql --username postgres << EOSQL
CREATE ROLE "${DATABASE_USERNAME}" LOGIN PASSWORD '${DATABASE_PASSWORD}';
CREATE DATABASE "${DATABASE_DATABASE}" OWNER "${DATABASE_USERNAME}";

\c "${DATABASE_DATABASE}";
SET ROLE "${DATABASE_USERNAME}";
\i createDatabase.sql;
EOSQL
