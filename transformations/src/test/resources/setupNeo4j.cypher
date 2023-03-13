MATCH (a)-[r]->() DELETE a, r;
MATCH (a) DELETE a;

CREATE (a: order {
    customer_id: '1',
    number: '2043'
});
CREATE (a: order {
    customer_id: '2',
    number: '1653'
});

CREATE (a: product {
    id: 'A1',
    name: 'Some name'
});
CREATE (a: product {
    id: 'B2',
    name: 'Other name'
});

MATCH (a: order { number: '2043' }), (b: product { id: 'A1' }) CREATE (a)-[r: items { quantity: '10' }]->(b);
MATCH (a: order { number: '2043' }), (b: product { id: 'A1' }) CREATE (a)-[r: items { quantity: '12' }]->(b);
MATCH (a: order { number: '2043' }), (b: product { id: 'B2' }) CREATE (a)-[r: items { quantity: '17' }]->(b);
MATCH (a: order { number: '1653' }), (b: product { id: 'B2' }) CREATE (a)-[r: items { quantity: '24' }]->(b);
