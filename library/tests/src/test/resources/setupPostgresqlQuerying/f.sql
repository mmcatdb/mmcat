DROP TABLE IF EXISTS "f";

CREATE TABLE "f" (
    "id" TEXT PRIMARY KEY,
    "value" TEXT
);

INSERT INTO "f" ("id", "value")
VALUES
    ('f_id:0', 'f_value:0'),
    ('f_id:1', 'f_value:1');

    -- ('f_id:1', 'f_value:1'),
    -- ('f_id:2', 'f_value:2'),
    -- ('f_id:3', 'f_value:3'),
    -- ('f_id:4', 'f_value:4'),
    -- ('f_id:5', 'f_value:5'),
    -- ('f_id:6', 'f_value:6'),
    -- ('f_id:7', 'f_value:7');
