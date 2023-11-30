DROP TABLE IF EXISTS "ordered";
DROP TABLE IF EXISTS "items";
DROP TABLE IF EXISTS "order2";
DROP TABLE IF EXISTS "order1";
DROP TABLE IF EXISTS "knows";
DROP TABLE IF EXISTS "customer";
DROP TABLE IF EXISTS "product";

CREATE TABLE "product"(
    "pid" TEXT PRIMARY KEY,
    "title" TEXT,
    "price" TEXT
);
INSERT INTO "product" ("pid", "title", "price")
VALUES
    ('P5', 'Sourcery', '350'),
    ('P7', 'Pyramids', '275');

CREATE TABLE "customer"(
    "id" TEXT PRIMARY KEY,
    "name" TEXT,
    "surname" TEXT
);
INSERT INTO "customer" ("id", "name", "surname")
VALUES
    ('1', 'Mary', 'Smith'),
    ('2', 'John', 'Newlin'),
    ('3', 'Anne', 'Maxwell');

CREATE TABLE "knows"(
    "id1" TEXT REFERENCES "customer",
    "id2" TEXT REFERENCES "customer",
    PRIMARY KEY ("id1", "id2")
);
INSERT INTO "knows" ("id1", "id2")
VALUES
    ('1', '2'),
    ('1', '3');

CREATE TABLE "order1"(
    "id" TEXT REFERENCES "customer",
    "pid" TEXT REFERENCES "product",
    "oid" TEXT,
    "price" TEXT,
    "quantity" TEXT,
    "street" TEXT,
    "city" TEXT,
    "postCode" TEXT,
    PRIMARY KEY ("pid", "oid")
);
INSERT INTO "order1" ("id", "pid", "oid", "price", "quantity", "street", "city", "postCode")
VALUES
    ('1', 'P5', '2023001', '350', '1', 'Ke Karlovu', 'Prague', '110 00'),
    ('1', 'P7', '2023001', '250', '1', 'Ke Karlovu', 'Prague', '110 00'),
    ('2', 'P7', '2023002', '275', '2', 'Technická', 'Prague', '162 00');

CREATE TABLE "order2"(
    "oid" TEXT PRIMARY KEY,
    "street" TEXT,
    "city" TEXT,
    "postCode" TEXT
);
INSERT INTO "order2" ("oid", "street", "city", "postCode")
VALUES
    ('2023001', 'Ke Karlovu', 'Prague', '110 00'),
    ('2023002', 'Technická', 'Prague', '162 00');

CREATE TABLE "items"(
    "pid" TEXT REFERENCES "product",
    "oid" TEXT REFERENCES "order2",
    "price" TEXT,
    "quantity" TEXT,
    PRIMARY KEY ("pid", "oid")
);
INSERT INTO "items" ("pid", "oid", "price", "quantity")
VALUES
    ('P5', '2023001', '350', '1'),
    ('P7', '2023001', '250', '1'),
    ('P7', '2023002', '275', '2');

CREATE TABLE "ordered"(
    "id" TEXT REFERENCES "customer",
    "oid" TEXT REFERENCES "order2",
    PRIMARY KEY ("id", "oid")
);
INSERT INTO "ordered" ("id", "oid")
VALUES
    ('1', '2023001'),
    ('2', '2023002');
