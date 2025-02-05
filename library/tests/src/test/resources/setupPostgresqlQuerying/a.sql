DROP TABLE IF EXISTS "a";

CREATE TABLE "a" (
    "id" TEXT PRIMARY KEY,
    "value" TEXT,
    "b_id" TEXT
);

INSERT INTO "a" ("id", "value", "b_id")
VALUES
    ('a_id:0', 'a_value:0', 'b_id:0'),
    ('a_id:1', 'a_value:1', 'b_id:1');

    -- ('a_id:1', 'a_value:1', 'b_id:1'),
    -- ('a_id:2', 'a_value:2', 'b_id:2'),
    -- ('a_id:3', 'a_value:3', 'b_id:3'),
    -- ('a_id:4', 'a_value:4', 'b_id:4'),
    -- ('a_id:5', 'a_value:5', 'b_id:5'),
    -- ('a_id:6', 'a_value:6', 'b_id:6'),
    -- ('a_id:7', 'a_value:7', 'b_id:7');
