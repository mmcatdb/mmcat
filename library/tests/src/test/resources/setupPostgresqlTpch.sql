DROP TABLE IF EXISTS "LineItem";
DROP TABLE IF EXISTS "Orders";

CREATE TABLE "Orders" (
    -- FIXME The types are not matching the ones in TPC-H specification but ...
    "orderkey" TEXT PRIMARY KEY,
    "orderstatus" TEXT,
    "totalprice" TEXT,
    "orderdate" TEXT,
    "orderpriority" TEXT,
    "shippriority" TEXT,
    "clerk" TEXT,
    "comment" TEXT,
    "custkey" TEXT
);

INSERT INTO "Orders" ("orderkey", "orderstatus", "totalprice", "orderdate", "orderpriority", "shippriority", "clerk", "comment", "custkey")
VALUES
    ('o_1', 'O', '173665.47', '1996-01-02', '5-LOW', '0', 'Clerk#000000951', 'comment_1', 'c_1');

CREATE TABLE "LineItem" (
    "linenumber" TEXT,
    "quantity" TEXT,
    "extendedprice" TEXT,
    "discount" TEXT,
    "tax" TEXT,
    "returnflag" TEXT,
    "linestatus" TEXT,
    "shipdate" TEXT,
    "commitdate" TEXT,
    "receiptdate" TEXT,
    "shipinstruct" TEXT,
    "shipmode" TEXT,
    "comment" TEXT,
    "orderkey" TEXT NOT NULL REFERENCES "Orders",
    "partkey" TEXT,
    "suppkey" TEXT,

    PRIMARY KEY ("orderkey", "linenumber")
);
