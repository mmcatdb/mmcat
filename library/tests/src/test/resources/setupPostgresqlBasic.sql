DROP TABLE IF EXISTS "order_item";
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS "product";
DROP TABLE IF EXISTS "dynamic";

-- The most simple order table for some tests. --
CREATE TABLE "order" (
    "number" TEXT PRIMARY KEY
);

INSERT INTO "order" ("number")
VALUES
    ('o_100'),
    ('o_200');

-- The order table for the structure test. --
CREATE TABLE "product" (
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

CREATE TABLE "order_item" (
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

CREATE TABLE "dynamic" (
    "id" TEXT PRIMARY KEY,
    "label" TEXT,
    "px_a" TEXT,
    "py_a" TEXT,
    "px_b" TEXT,
    "py_b" TEXT,
    "catch_all_a" TEXT,
    "catch_all_b" TEXT
);

INSERT INTO "dynamic" ("id", "label", "px_a", "py_a", "px_b", "py_b", "catch_all_a", "catch_all_b")
VALUES
    ('id-0', 'label-0', 'px-a-0', 'py-a-0', 'px-b-0', 'py-b-0', 'catch-all-a-0', 'catch-all-b-0'),
    ('id-1', 'label-1', 'px-a-1', 'py-a-1', 'px-b-1', 'py-b-1', 'catch-all-a-1', 'catch-all-b-1');
