MATCH (a)-[r]->() DELETE a, r;
MATCH (a) DELETE a;

CREATE (a:Order { customer: 'Alice', number: 'o_100' });
CREATE (a:Order { customer: 'Bob', number: 'o_200' });

CREATE (a:Product { id: 'A1', label: 'Some name' });
CREATE (a:Product { id: 'B2', label: 'Other name' });

MATCH (a:Order { number: 'o_100' }), (b:Product { id: 'A1' }) CREATE (a)-[r:ITEM { quantity: '10' }]->(b);
MATCH (a:Order { number: 'o_100' }), (b:Product { id: 'A1' }) CREATE (a)-[r:ITEM { quantity: '12' }]->(b);
MATCH (a:Order { number: 'o_100' }), (b:Product { id: 'B2' }) CREATE (a)-[r:ITEM { quantity: '17' }]->(b);
MATCH (a:Order { number: 'o_200' }), (b:Product { id: 'B2' }) CREATE (a)-[r:ITEM { quantity: '24' }]->(b);
