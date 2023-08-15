FROM postgres:latest

COPY docker/database/entrypoint.sh docker-entrypoint-initdb.d/entrypoint.sh
COPY docker/database/*.sql .
