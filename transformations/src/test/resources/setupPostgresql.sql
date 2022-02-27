-- The most simple order table for some tests. --
DROP TABLE IF EXISTS "order";
CREATE TABLE "order"(
    "number" TEXT PRIMARY KEY
);

INSERT INTO "order" ("number")
VALUES
    ('2043'),
    ('1653');

-- The order table for the basic test. --
DROP TABLE IF EXISTS "order_basic";
CREATE TABLE "order_basic"(
    "number" TEXT PRIMARY KEY
);

INSERT INTO "order_basic" ("number")
VALUES
    ('2043'),
    ('1653');

-- The order table for the nested structure test. --
DROP TABLE IF EXISTS "order_structure";
CREATE TABLE "order_structure"(
    "number" TEXT PRIMARY KEY,
    "nested/propertyA" TEXT,
    "nested/propertyB" TEXT,
    "nested/propertyC" TEXT
);

INSERT INTO "order_structure" ("number", "nested/propertyA", "nested/propertyB", "nested/propertyC")
VALUES
    ('2043', 'hodnota', 'hodnota', 'hodnota'),
    ('1653', 'hodnotaA', 'hodnotaB', 'hodnotaC');
