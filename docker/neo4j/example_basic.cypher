MATCH (a)-[r]->() DELETE a, r;
MATCH (a) DELETE a;

CREATE (a: Customer {
    customer_id: '1'
});
CREATE (a: Customer {
    customer_id: '2'
});

MATCH (a: Customer { customer_id: '1' }), (b: Customer { customer_id: '2' }) CREATE (a)-[r: Friend { since: 'yesterday' }]->(b);
MATCH (a: Customer { customer_id: '1' }), (b: Customer { customer_id: '1' }) CREATE (a)-[r: Friend { since: 'forever' }]->(b);