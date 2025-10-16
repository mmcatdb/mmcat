MATCH (a:Order)-[r]-() DELETE a, r;
MATCH (a:Product)-[r]-() DELETE a, r;
MATCH (a:Note)-[r]-() DELETE a, r;

CREATE (a:Order { customer: 'Alice', number: 'o_100' });
CREATE (a:Order { customer: 'Bob', number: 'o_200' });

CREATE (a:Product { id: 'A1', label: 'Some name' });
CREATE (a:Product { id: 'B2', label: 'Other name' });

MATCH (a:Order { number: 'o_100' }), (b:Product { id: 'A1' }) CREATE (a)-[r:ITEM { quantity: '10' }]->(b);
MATCH (a:Order { number: 'o_100' }), (b:Product { id: 'A1' }) CREATE (a)-[r:ITEM { quantity: '12' }]->(b);
MATCH (a:Order { number: 'o_100' }), (b:Product { id: 'B2' }) CREATE (a)-[r:ITEM { quantity: '17' }]->(b);
MATCH (a:Order { number: 'o_200' }), (b:Product { id: 'B2' }) CREATE (a)-[r:ITEM { quantity: '24' }]->(b);

MATCH (a:Order { number: 'o_100' }) CREATE
    (a)<-[:NOTE_REL { locale: 'en-US' }]-(:Note { subject: 'subject 1', content: 'content en' }),
    (a)<-[:NOTE_REL { locale: 'cs-CZ' }]-(:Note { subject: 'subject 1', content: 'content cz' });

MATCH (a:Order { number: 'o_200' }) CREATE
    (a)<-[:NOTE_REL { locale: 'cs-CZ' }]-(:Note { subject: 'subject cz', content: 'content 1' }),
    (a)<-[:NOTE_REL { locale: 'en-GB' }]-(:Note { subject: 'subject gb', content: 'content 2' });
