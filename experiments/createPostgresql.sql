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
