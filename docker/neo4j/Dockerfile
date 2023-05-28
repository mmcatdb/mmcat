FROM neo4j:latest

COPY docker/neo4j/entrypoint-wrapper.sh entrypoint-wrapper.sh
COPY docker/neo4j/entrypoint.sh entrypoint.sh
COPY docker/neo4j/*.cypher .

# This doesn't work, because the neo4j crew are morons and the script would be executed before the DB is started.
# ENV EXTENSION_SCRIPT=entrypoint.sh

RUN chmod +x entrypoint-wrapper.sh
ENTRYPOINT [ "tini", "-g", "--", "./entrypoint-wrapper.sh" ]
CMD [ "neo4j" ]