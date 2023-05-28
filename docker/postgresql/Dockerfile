FROM postgres:latest

COPY docker/postgresql/entrypoint.sh docker-entrypoint-initdb.d/entrypoint.sh
COPY docker/postgresql/*.sql .
