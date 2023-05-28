DROP TABLE IF EXISTS app_order_item;
DROP TABLE IF EXISTS app_product;
DROP TABLE IF EXISTS app_order;
DROP TABLE IF EXISTS app_customer_contact;
DROP TABLE IF EXISTS app_customer;
DROP TABLE IF EXISTS app_contact;

CREATE TABLE app_contact (
    id TEXT PRIMARY KEY,
    type TEXT,
    value TEXT
);

CREATE TABLE app_customer (
    id TEXT PRIMARY KEY,
    full_name TEXT
);

CREATE TABLE app_customer_contact (
    customer_id TEXT,
    contact_id TEXT,
    PRIMARY KEY (customer_id, contact_id)
);

CREATE TABLE app_order (
    id TEXT PRIMARY KEY,
    customer_id TEXT REFERENCES app_customer(id),
    created TEXT,
    sent TEXT,
    paid TEXT,
    note TEXT,
    delivery_address TEXT
);

CREATE TABLE app_product (
    id TEXT PRIMARY KEY,
    name TEXT,
    price TEXT
);

CREATE TABLE app_order_item (
    order_id TEXT,
    product_id TEXT,
    amount TEXT,
    total_price TEXT,
    PRIMARY KEY (order_id, product_id)
);

INSERT INTO app_contact (id, type, value)
VALUES
    ('1', 'email', 'contact 1'),
    ('2', 'phone', 'contact 2'),
    ('3', 'email', 'contact 3');

INSERT INTO app_customer (id, full_name)
VALUES
    ('1', 'Alice'),
    ('2', 'Bob');

INSERT INTO app_customer_contact (customer_id, contact_id)
VALUES
    ('1', '1'),
    ('1', '2'),
    ('2', '3');

INSERT INTO app_order (id, customer_id, created, sent, paid, note, delivery_address)
VALUES
    ('1', '1', 'A', 'B', 'C', '', 'address 1'),
    ('2', '1', 'A', 'B', 'C', '', 'address 1'),
    ('3', '2', 'A', 'B', 'C', '', 'address 2'),
    ('4', '2', 'A', 'B', 'C', '', 'address 3');

INSERT INTO app_product (id, name, price)
VALUES
    ('1', 'product 1', '1000'),
    ('2', 'product 2', '2000'),
    ('3', 'product 3', '3000'),
    ('4', 'product 4', '4000'),
    ('5', 'product 5', '5000'),
    ('6', 'product 6', '6000');

INSERT INTO app_order_item (order_id, product_id, amount, total_price)
VALUES
    ('1', '1', '5', '5000'),
    ('1', '2', '10', '20000'),
    ('2', '1', '1', '1000'),
    ('3', '1', '2', '2000'),
    ('4', '1', '3', '3000');