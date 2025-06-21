DROP TABLE IF EXISTS "c";

CREATE TABLE "c" (
    "id" TEXT PRIMARY KEY,
    "value" TEXT,
    "d_id" TEXT
);

INSERT INTO "c" ("id", "value", "d_id")
VALUES
    ('c_id:0', 'c_value:0', 'd_id:0'),
    ('c_id:1', 'c_value:1', 'd_id:1');

    -- ('c_id:1', 'c_value:1', 'd_id:1'),
    -- ('c_id:2', 'c_value:2', 'd_id:2'),
    -- ('c_id:3', 'c_value:3', 'd_id:3'),
    -- ('c_id:4', 'c_value:4', 'd_id:4'),
    -- ('c_id:5', 'c_value:5', 'd_id:5'),
    -- ('c_id:6', 'c_value:6', 'd_id:6'),
    -- ('c_id:7', 'c_value:7', 'd_id:7');
