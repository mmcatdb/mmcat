FROM mongo:latest

COPY docker/mongodb/entrypoint.sh docker-entrypoint-initdb.d/entrypoint.sh
COPY library/tests/src/test/resources/setupMongodb.js .
