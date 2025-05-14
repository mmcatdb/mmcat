DROP TABLE IF EXISTS "d";

CREATE TABLE "d" (
    "id" TEXT PRIMARY KEY,
    "value" TEXT,
    "e_id" TEXT
);

INSERT INTO "d" ("id", "value", "e_id")
VALUES
    ('d_id:0', 'd_value:0', 'e_id:0'),
    ('d_id:1', 'd_value:1', 'e_id:1');

    -- ('d_id:1', 'd_value:1', 'e_id:1'),
    -- ('d_id:2', 'd_value:2', 'e_id:2'),
    -- ('d_id:3', 'd_value:3', 'e_id:3'),
    -- ('d_id:4', 'd_value:4', 'e_id:4'),
    -- ('d_id:5', 'd_value:5', 'e_id:5'),
    -- ('d_id:6', 'd_value:6', 'e_id:6'),
    -- ('d_id:7', 'd_value:7', 'e_id:7');
