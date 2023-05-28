#!/bin/bash

# psql "postgresql://${DB_USERNAME}:${DB_PASSWORD}@localhost/${DB_DATABASE}?sslmode=require" -f src/main/resources/createDatabase.sql

psql --username postgres << EOSQL
CREATE ROLE ${DB_USERNAME} LOGIN PASSWORD '${DB_PASSWORD}';
CREATE DATABASE ${DB_DATABASE} OWNER ${DB_USERNAME};
\c ${DB_DATABASE};
SET ROLE ${DB_USERNAME};
\set db_example_username ${DB_EXAMPLE_USERNAME}
\set db_example_password ${DB_EXAMPLE_PASSWORD}
\i createDatabase.sql;

SET ROLE postgres;
CREATE ROLE ${DB_EXAMPLE_USERNAME} LOGIN PASSWORD '${DB_EXAMPLE_PASSWORD}';
CREATE DATABASE mm_example_basic OWNER ${DB_EXAMPLE_USERNAME};
CREATE DATABASE mm_example_ttd OWNER ${DB_EXAMPLE_USERNAME};
CREATE DATABASE mm_example_query OWNER ${DB_EXAMPLE_USERNAME};

\c mm_example_basic;
SET ROLE ${DB_EXAMPLE_USERNAME};
\i example_basic.sql;

\c mm_example_ttd;
SET ROLE ${DB_EXAMPLE_USERNAME};
\i example_ttd.sql;

\c mm_example_query;
SET ROLE ${DB_EXAMPLE_USERNAME};
\i example_query.sql;
EOSQL
