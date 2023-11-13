FROM postgres:latest

COPY docker/postgresql/entrypoint.sh docker-entrypoint-initdb.d/entrypoint.sh
COPY library/tests/src/test/resources/setupPostgresql.sql .
