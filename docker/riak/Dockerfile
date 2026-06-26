FROM basho/riak-kv:latest

COPY docker/riak/entrypoint.sh /etc/riak/poststart.d/entrypoint.sh
COPY library/tests/src/test/resources/setupRiakBasic.erl .
