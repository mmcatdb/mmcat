MATCH (a:Order) DETACH DELETE a;
MATCH (a:Product) DETACH DELETE a;
MATCH (a:Contact) DETACH DELETE a;
MATCH (a:Note) DETACH DELETE a;


CREATE (a:Order { customer: 'Alice', number: 'o_100' });
CREATE (a:Order { customer: 'Bob', number: 'o_200' });

CREATE (a:Product { id: '123', label: 'Clean Code', price: '125' });
CREATE (a:Product { id: '765', label: 'The Lord of the Rings', price: '199' });
CREATE (a:Product { id: '457', label: 'The Art of War', price: '299' });
CREATE (a:Product { id: '734', label: 'Animal Farm', price: '350' });

MATCH (a:Order { number: 'o_100' }), (b:Product { id: '123' }) CREATE (a)-[r:ITEM { quantity: '1' }]->(b);
MATCH (a:Order { number: 'o_100' }), (b:Product { id: '765' }) CREATE (a)-[r:ITEM { quantity: '2' }]->(b);
MATCH (a:Order { number: 'o_200' }), (b:Product { id: '457' }) CREATE (a)-[r:ITEM { quantity: '7' }]->(b);
MATCH (a:Order { number: 'o_200' }), (b:Product { id: '734' }) CREATE (a)-[r:ITEM { quantity: '3' }]->(b);

MATCH (a:Order { number: 'o_100' }) CREATE
    (a)-[:HAS_CONTACT { type: 'phone' }]->(:Contact { value: '123456789' }),
    (a)-[:HAS_CONTACT { type: 'email' }]->(:Contact { value: 'alice@mmcatdb.com' });
MATCH (a:Order { number: 'o_200' }) CREATE
    (a)-[:HAS_CONTACT { type: 'email' }]->(:Contact { value: 'bob@mmcactdb.com'}),
    (a)-[:HAS_CONTACT { type: 'github' }]->(:Contact { value: 'https://github.com/mmcactdb' });
