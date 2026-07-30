#!/bin/bash

# TODO: enable authentication

riak-admin bucket-type create example

riak-admin bucket-type activate example

/etc/riak/setupRiak.sh http://${HOST}:${HTTP_PORT} example
