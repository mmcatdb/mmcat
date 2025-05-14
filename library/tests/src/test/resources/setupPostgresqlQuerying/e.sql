DROP TABLE IF EXISTS "e";

CREATE TABLE "e" (
    "id" TEXT PRIMARY KEY,
    "value" TEXT,
    "f_id" TEXT
);

INSERT INTO "e" ("id", "value", "f_id")
VALUES
    ('e_id:0', 'e_value:0', 'f_id:0'),
    ('e_id:1', 'e_value:1', 'f_id:1');

    -- ('e_id:1', 'e_value:1', 'f_id:1'),
    -- ('e_id:2', 'e_value:2', 'f_id:2'),
    -- ('e_id:3', 'e_value:3', 'f_id:3'),
    -- ('e_id:4', 'e_value:4', 'f_id:4'),
    -- ('e_id:5', 'e_value:5', 'f_id:5'),
    -- ('e_id:6', 'e_value:6', 'f_id:6'),
    -- ('e_id:7', 'e_value:7', 'f_id:7');
