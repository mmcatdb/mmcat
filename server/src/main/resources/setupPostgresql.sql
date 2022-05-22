DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
    id TEXT PRIMARY KEY
);

INSERT INTO customer (id)
VALUES
    ('1'),
    ('2');

DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
    customer_id TEXT,
    number TEXT,
    PRIMARY KEY (customer_id, number)
);

INSERT INTO orders (customer_id, number)
VALUES
    ('1', '1'),
    ('1', '2'),
    ('2', '1'),
    ('2', '2');
