import json
import sys
from time import sleep

def create_response(index):
    objexes = [ {
        'key': 'user',
        'mappings': [ 'postgres' ],
    }, {
        'key': 'product',
        'mappings': [ 'neo4j', 'mongo' ],
    } ]

    queries = [ {
        'id': 'test:0',
        'cost': 1
    }, {
        'id': 'test:1',
        'cost': 2
    } ]

    solutions = [ {
        'id': index,
        'price': 0.5,
        'objexes': objexes,
        'queries': queries,
    }, {
        'id': index + 1,
        'price': 1.5,
        'objexes': objexes,
        'queries': queries,
    } ]

    return {
        'processedStates': index * 2,
        'solutions': solutions,
    }

i = 0

while True:
    response = create_response(i)
    i += 1

    json_string = json.dumps(response)
    sys.stdout.write(json_string + '\n')
    sys.stdout.flush()

    sleep(1)
