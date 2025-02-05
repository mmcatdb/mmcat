DROP TABLE IF EXISTS "b";

CREATE TABLE "b" (
    "id" TEXT PRIMARY KEY,
    "value" TEXT,
    "c_id" TEXT
);

INSERT INTO "b" ("id", "value", "c_id")
VALUES
    ('b_id:0', 'b_value:0', 'c_id:0'),
    ('b_id:1', 'b_value:1', 'c_id:1');

    -- ('b_id:1', 'b_value:1', 'c_id:1'),
    -- ('b_id:2', 'b_value:2', 'c_id:2'),
    -- ('b_id:3', 'b_value:3', 'c_id:3'),
    -- ('b_id:4', 'b_value:4', 'c_id:4'),
    -- ('b_id:5', 'b_value:5', 'c_id:5'),
    -- ('b_id:6', 'b_value:6', 'c_id:6'),
    -- ('b_id:7', 'b_value:7', 'c_id:7');
