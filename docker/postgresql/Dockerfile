FROM postgres:latest

COPY docker/postgresql/entrypoint.sh docker-entrypoint-initdb.d/entrypoint.sh
COPY library/tests/src/test/resources/setupPostgresqlBasic.sql .
COPY library/tests/src/test/resources/setupPostgresqlAdminer.sql .
COPY library/tests/src/test/resources/setupPostgresqlQueryEvolution.sql .
COPY library/tests/src/test/resources/setupPostgresqlQuerying .
