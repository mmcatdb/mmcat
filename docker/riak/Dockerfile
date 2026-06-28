FROM basho/riak-kv:latest

COPY docker/riak/entrypoint.sh /etc/riak/poststart.d/entrypoint.sh
COPY docker/riak/riak.conf /etc/riak/riak.conf

# TODO: incorporate riak into basic, adminer, inference, querying, queryEvolution,... test schemas
COPY library/tests/src/test/resources/setupRiak.sh /etc/riak/setupRiak.sh

RUN chmod +x /etc/riak/setupRiak.sh
