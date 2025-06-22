FROM mongo:latest

COPY docker/mongodb/entrypoint.sh docker-entrypoint-initdb.d/entrypoint.sh
COPY library/tests/src/test/resources/setupMongodbBasic.js .
COPY library/tests/src/test/resources/setupMongodbAdminer.js .
COPY library/tests/src/test/resources/setupMongodbQueryEvolution.js .
COPY library/tests/src/test/resources/setupMongodbInference.js .
