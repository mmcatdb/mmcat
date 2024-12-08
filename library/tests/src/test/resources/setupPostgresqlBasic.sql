-- The most simple order table for some tests. --
DROP TABLE IF EXISTS "order";
CREATE TABLE "order"(
    "number" TEXT PRIMARY KEY
);

INSERT INTO "order" ("number")
VALUES
    ('o_100'),
    ('o_200');

-- The order table for the structure test. --
DROP TABLE IF EXISTS "product";
CREATE TABLE "product"(
    "id" TEXT PRIMARY KEY,
    "label" TEXT,
    "price" TEXT
);

INSERT INTO "product" ("id", "label", "price")
VALUES
    ('123', 'Clean Code', '125'),
    ('765', 'The Lord of the Rings', '199'),
    ('457', 'The Art of War', '299'),
    ('734', 'Animal Farm', '350');

DROP TABLE IF EXISTS "order_item";
CREATE TABLE "order_item"(
    "order_number" TEXT,
    "product_id" TEXT,
    "quantity" TEXT,
    PRIMARY KEY ("order_number", "product_id"),
    CONSTRAINT fk_order FOREIGN KEY ("order_number") REFERENCES "order" ("number") ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_product FOREIGN KEY ("product_id") REFERENCES "product" ("id") ON DELETE SET NULL ON UPDATE CASCADE
);

INSERT INTO "order_item" ("order_number", "product_id", "quantity")
VALUES
    ('o_100', '123', '1'),
    ('o_100', '765', '2'),
    ('o_200', '457', '7'),
    ('o_200', '734', '3');
