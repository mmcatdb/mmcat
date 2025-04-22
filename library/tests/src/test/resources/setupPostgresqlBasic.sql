-- The most simple order table for some tests. --
DROP TABLE IF EXISTS "order";
CREATE TABLE "order" (
    "number" TEXT PRIMARY KEY
);

INSERT INTO "order" ("number")
VALUES
    ('o_100'),
    ('o_200');

-- The order table for the structure test. --
DROP TABLE IF EXISTS "product";
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

DROP TABLE IF EXISTS "order_item";
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

DROP TABLE IF EXISTS "dynamic";
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

-- Yelp data --
DROP TABLE IF EXISTS "user";
CREATE TABLE "user" (
  "user_id" VARCHAR(50) PRIMARY KEY,
  "name" VARCHAR(100),
  "created_at" DATE NOT NULL,
  "fans" INTEGER DEFAULT 0
);

INSERT INTO "user" ("user_id", "name", "created_at", "fans")
VALUES
    ('user_001', 'Bob Smith', '2020-07-19', 21),
    ('user_002', 'Carol Johnson', '2021-05-26', 7),
    ('user_003', 'Bob Smith', '2022-08-18', 4),
    ('user_004', 'Dave Johnson', '2021-09-04', 8),
    ('user_005', 'Carol Taylor', '2022-09-23', 73),
    ('user_006', 'Bob Smith', '2019-03-15', 100),
    ('user_007', 'Eve Lee', '2021-01-15', 46),
    ('user_008', 'Dave Johnson', '2020-10-04', 4),
    ('user_009', 'Carol Lee', '2020-01-06', 68),
    ('user_010', 'Alice Johnson', '2019-06-04', 96),
    ('user_011', 'Alice Johnson', '2020-01-30', 94),
    ('user_012', 'Dave Smith', '2019-05-09', 95),
    ('user_013', 'Alice Smith', '2022-03-21', 4),
    ('user_014', 'Eve Smith', '2019-01-10', 15),
    ('user_015', 'Dave Lee', '2022-05-09', 11),
    ('user_016', 'Dave Lee', '2020-10-12', 45),
    ('user_017', 'Bob Lee', '2022-05-06', 45),
    ('user_018', 'Alice Smith', '2020-09-17', 16),
    ('user_019', 'Carol Johnson', '2020-01-18', 95),
    ('user_020', 'Carol Lee', '2020-08-19', 8),
    ('user_021', 'Eve Taylor', '2020-10-12', 30),
    ('user_022', 'Bob Lee', '2021-05-05', 46),
    ('user_023', 'Alice Lee', '2020-04-26', 28),
    ('user_024', 'Eve Johnson', '2022-07-30', 39),
    ('user_025', 'Eve Johnson', '2020-04-11', 8),
    ('user_026', 'Carol Smith', '2022-01-09', 71),
    ('user_027', 'Dave Lee', '2022-01-01', 68),
    ('user_028', 'Carol Johnson', '2022-04-14', 57),
    ('user_029', 'Bob Taylor', '2020-10-15', 9),
    ('user_030', 'Carol Taylor', '2019-08-09', 77),
    ('user_031', 'Alice Taylor', '2021-02-11', 5),
    ('user_032', 'Eve Smith', '2020-11-03', 64),
    ('user_033', 'Dave Johnson', '2022-02-19', 12),
    ('user_034', 'Carol Smith', '2021-06-06', 33),
    ('user_035', 'Bob Johnson', '2022-12-25', 48),
    ('user_036', 'Alice Lee', '2020-03-14', 79),
    ('user_037', 'Carol Lee', '2019-07-22', 6),
    ('user_038', 'Dave Smith', '2023-01-05', 14),
    ('user_039', 'Eve Taylor', '2020-06-28', 54),
    ('user_040', 'Bob Taylor', '2021-12-18', 81),
    ('user_041', 'Alice Johnson', '2022-03-03', 22),
    ('user_042', 'Bob Smith', '2023-04-09', 13),
    ('user_043', 'Carol Taylor', '2020-12-11', 7),
    ('user_044', 'Dave Lee', '2019-02-20', 99),
    ('user_045', 'Eve Johnson', '2021-07-29', 41),
    ('user_046', 'Alice Taylor', '2022-06-07', 37),
    ('user_047', 'Bob Lee', '2023-03-08', 66),
    ('user_048', 'Eve Smith', '2019-12-01', 17),
    ('user_049', 'Carol Johnson', '2021-11-14', 5),
    ('user_050', 'Dave Smith', '2020-02-05', 19),
    ('user_051', 'Alice Lee', '2022-10-23', 38),
    ('user_052', 'Bob Taylor', '2021-01-17', 60),
    ('user_053', 'Carol Smith', '2019-04-27', 26),
    ('user_054', 'Eve Lee', '2020-05-19', 40),
    ('user_055', 'Dave Johnson', '2021-09-30', 71),
    ('user_056', 'Alice Johnson', '2023-02-11', 9),
    ('user_057', 'Bob Lee', '2022-09-16', 88),
    ('user_058', 'Eve Taylor', '2021-03-01', 34),
    ('user_059', 'Carol Taylor', '2020-01-22', 25),
    ('user_060', 'Dave Lee', '2022-08-02', 20);

DROP TABLE IF EXISTS "comment";
CREATE TABLE "comment" (
  "id" VARCHAR(50) PRIMARY KEY,
  "user_id" VARCHAR(50) NOT NULL REFERENCES "user"("user_id"),
  "business_id" VARCHAR(50) NOT NULL,
  "date" DATE NOT NULL,
  "text" TEXT,
  "stars" INTEGER CHECK (stars BETWEEN 1 AND 5)
);

INSERT INTO "comment" ("id", "user_id", "business_id", "date", "text", "stars")
VALUES
    ('comm_001', 'user_001', 'buss_014', '2023-04-10', 'Great service!', 2),
    ('comm_002', 'user_004', 'buss_019', '2021-04-21', 'Not so good', NULL),
    ('comm_003', 'user_003', 'buss_001', '2023-05-27', 'Not so good', NULL),
    ('comm_004', 'user_027', 'buss_013', '2022-11-01', NULL, 3),
    ('comm_005', 'user_021', 'buss_030', '2023-07-02', 'Loved it!', 1),
    ('comm_006', 'user_029', 'buss_018', '2020-03-13', 'Loved it!', NULL),
    ('comm_007', 'user_015', 'buss_007', '2022-07-31', 'Great service!', NULL),
    ('comm_008', 'user_004', 'buss_028', '2021-11-12', NULL, NULL),
    ('comm_009', 'user_029', 'buss_016', '2021-05-02', 'Loved it!', 5),
    ('comm_010', 'user_017', 'buss_022', '2021-05-27', 'Okay experience', NULL),
    ('comm_011', 'user_024', 'buss_001', '2022-11-26', NULL, NULL),
    ('comm_012', 'user_027', 'buss_013', '2020-03-03', 'Okay experience', 3),
    ('comm_013', 'user_006', 'buss_011', '2021-01-16', 'Loved it!', NULL),
    ('comm_014', 'user_008', 'buss_028', '2020-01-02', 'Okay experience', NULL),
    ('comm_015', 'user_004', 'buss_030', '2023-02-19', 'Not so good', NULL);

DROP TABLE IF EXISTS "business_hours";
CREATE TABLE "business_hours" (
    "id" SERIAL PRIMARY KEY,
    "business_id" VARCHAR(50) NOT NULL,
    "hours" JSONB NOT NULL
);
INSERT INTO "business_hours" ("business_id", "hours") VALUES
(
  'buss_001',
  '{
    "monday": {"open": "09:00", "close": "17:00"},
    "tuesday": {"open": "09:00", "close": "17:00"},
    "wednesday": {"open": "09:00", "close": "17:00"},
    "thursday": {"open": "10:00", "close": "18:00"},
    "friday": {"open": "09:00", "close": "16:00"},
    "saturday": null,
    "sunday": null
  }'
),
(
  'buss_002',
  '{
    "monday": {"open": "08:00", "close": "20:00"},
    "tuesday": {"open": "08:00", "close": "20:00"},
    "wednesday": {"open": "08:00", "close": "20:00"},
    "thursday": {"open": "08:00", "close": "20:00"},
    "friday": {"open": "08:00", "close": "22:00"},
    "saturday": {"open": "10:00", "close": "22:00"},
    "sunday": {"open": "10:00", "close": "18:00"}
  }'
),
(
  'buss_003',
  '{
    "monday": null,
    "tuesday": {"open": "11:00", "close": "15:00"},
    "wednesday": {"open": "11:00", "close": "15:00"},
    "thursday": null,
    "friday": {"open": "11:00", "close": "23:00"},
    "saturday": {"open": "14:00", "close": "23:00"},
    "sunday": {"open": "14:00", "close": "20:00"}
  }'
),
(
    'buss_004',
    '{"monday": {"open": "11:00", "close": "18:00"},
    "tuesday": {"open": "08:00", "close": "19:00"},
    "wednesday": {"open": "10:00", "close": "21:00"},
    "thursday": {"open": "11:00", "close": "19:00"},
    "friday": {"open": "06:00", "close": "21:00"},
    "saturday": null,
    "sunday": {"open": "09:00", "close": "16:00"}}'
),
(
    'buss_005',
    '{"monday": {"open": "07:00", "close": "18:00"},
    "tuesday": {"open": "08:00", "close": "17:00"},
    "wednesday": null,
    "thursday": {"open": "06:00", "close": "17:00"},
    "friday": {"open": "09:00", "close": "23:00"},
    "saturday": null,
    "sunday": {"open": "06:00", "close": "20:00"}}'
),
(
    'buss_006',
    '{"monday": {"open": "06:00", "close": "20:00"},
    "tuesday": {"open": "11:00", "close": "19:00"},
    "wednesday": {"open": "08:00", "close": "19:00"},
    "thursday": null,
    "friday": {"open": "07:00", "close": "18:00"},
    "saturday": {"open": "06:00", "close": "22:00"},
    "sunday": {"open": "06:00", "close": "22:00"}}'
),
(
    'buss_007',
    '{"monday": {"open": "08:00", "close": "21:00"},
    "tuesday": {"open": "06:00", "close": "21:00"},
    "wednesday": {"open": "12:00", "close": "21:00"},
    "thursday": {"open": "06:00", "close": "16:00"},
    "friday": {"open": "11:00", "close": "20:00"},
    "saturday": {"open": "12:00", "close": "18:00"},
    "sunday": null}'
),
(
    'buss_008',
    '{"monday": {"open": "07:00", "close": "19:00"},
    "tuesday": {"open": "11:00", "close": "18:00"},
    "wednesday": {"open": "12:00", "close": "20:00"},
    "thursday": {"open": "08:00", "close": "17:00"},
    "friday": {"open": "07:00", "close": "19:00"},
    "saturday": null,
    "sunday": {"open": "10:00", "close": "23:00"}}'
),
(
    'buss_009',
    '{"monday": {"open": "09:00", "close": "22:00"},
    "tuesday": null,
    "wednesday": {"open": "06:00", "close": "23:00"},
    "thursday": {"open": "07:00", "close": "16:00"},
    "friday": {"open": "12:00", "close": "16:00"},
    "saturday": {"open": "11:00", "close": "21:00"},
    "sunday": {"open": "08:00", "close": "18:00"}}'
),
(
    'buss_010',
    '{"monday": {"open": "07:00", "close": "20:00"},
    "tuesday": {"open": "09:00", "close": "20:00"},
    "wednesday": {"open": "08:00", "close": "21:00"},
    "thursday": null,
    "friday": {"open": "07:00", "close": "23:00"},
    "saturday": {"open": "10:00", "close": "23:00"},
    "sunday": {"open": "07:00", "close": "19:00"}}'
),
(
    'buss_011',
    '{"monday": {"open": "11:00", "close": "18:00"},
    "tuesday": {"open": "08:00", "close": "23:00"},
    "wednesday": {"open": "09:00", "close": "19:00"},
    "thursday": {"open": "07:00", "close": "23:00"},
    "friday": {"open": "06:00", "close": "18:00"},
    "saturday": {"open": "08:00", "close": "22:00"},
    "sunday": {"open": "12:00", "close": "17:00"}}'
),
(
    'buss_012',
    '{"monday": {"open": "09:00", "close": "23:00"},
    "tuesday": null,
    "wednesday": {"open": "11:00", "close": "19:00"},
    "thursday": {"open": "12:00", "close": "19:00"},
    "friday": {"open": "11:00", "close": "21:00"},
    "saturday": {"open": "06:00", "close": "23:00"},
    "sunday": null}'
),
(
    'buss_013',
    '{"monday": {"open": "12:00", "close": "23:00"},
    "tuesday": null,
    "wednesday": null,
    "thursday": {"open": "08:00", "close": "19:00"},
    "friday": {"open": "09:00", "close": "17:00"},
    "saturday": {"open": "08:00", "close": "22:00"},
    "sunday": null}'
),
(
    'buss_014',
    '{"monday": {"open": "08:00", "close": "21:00"},
    "tuesday": {"open": "09:00", "close": "22:00"},
    "wednesday": {"open": "09:00", "close": "22:00"},
    "thursday": {"open": "10:00", "close": "21:00"},
    "friday": {"open": "10:00", "close": "20:00"},
    "saturday": {"open": "12:00", "close": "21:00"},
    "sunday": {"open": "11:00", "close": "18:00"}}'
),
(
    'buss_015',
    '{"monday": {"open": "11:00", "close": "21:00"},
    "tuesday": null,
    "wednesday": {"open": "12:00", "close": "19:00"},
    "thursday": {"open": "12:00", "close": "23:00"},
    "friday": {"open": "10:00", "close": "21:00"},
    "saturday": {"open": "11:00", "close": "23:00"},
    "sunday": null}'
),
(
    'buss_016',
    '{"monday": {"open": "09:00", "close": "20:00"},
    "tuesday": {"open": "08:00", "close": "19:00"},
    "wednesday": null,
    "thursday": {"open": "08:00", "close": "17:00"},
    "friday": null,
    "saturday": {"open": "10:00", "close": "17:00"},
    "sunday": {"open": "09:00", "close": "17:00"}}'
),
(
    'buss_017',
    '{"monday": null,
    "tuesday": {"open": "06:00", "close": "20:00"},
    "wednesday": {"open": "09:00", "close": "19:00"},
    "thursday": {"open": "11:00", "close": "21:00"},
    "friday": {"open": "07:00", "close": "22:00"},
    "saturday": {"open": "07:00", "close": "23:00"},
    "sunday": {"open": "10:00", "close": "16:00"}}'
),
(
    'buss_018',
    '{"monday": {"open": "07:00", "close": "17:00"},
    "tuesday": {"open": "10:00", "close": "18:00"},
    "wednesday": {"open": "06:00", "close": "21:00"},
    "thursday": {"open": "10:00", "close": "17:00"},
    "friday": {"open": "09:00", "close": "19:00"},
    "saturday": {"open": "11:00", "close": "17:00"},
    "sunday": {"open": "09:00", "close": "22:00"}}'
),
(
    'buss_019',
    '{"monday": {"open": "06:00", "close": "22:00"},
    "tuesday": {"open": "06:00", "close": "20:00"},
    "wednesday": {"open": "12:00", "close": "21:00"},
    "thursday": {"open": "12:00", "close": "18:00"},
    "friday": {"open": "12:00", "close": "21:00"},
    "saturday": {"open": "06:00", "close": "20:00"},
    "sunday": {"open": "12:00", "close": "21:00"}}'
),
(
    'buss_020',
    '{"monday": {"open": "12:00", "close": "17:00"},
    "tuesday": {"open": "08:00", "close": "17:00"},
    "wednesday": {"open": "08:00", "close": "21:00"},
    "thursday": {"open": "06:00", "close": "17:00"},
    "friday": {"open": "06:00", "close": "21:00"},
    "saturday": null,
    "sunday": {"open": "10:00", "close": "22:00"}}'
),
(
    'buss_021',
    '{"monday": {"open": "07:00", "close": "22:00"},
    "tuesday": {"open": "12:00", "close": "16:00"},
    "wednesday": {"open": "08:00", "close": "18:00"},
    "thursday": {"open": "07:00", "close": "16:00"},
    "friday": {"open": "12:00", "close": "16:00"},
    "saturday": null,
    "sunday": {"open": "10:00", "close": "16:00"}}'
),
(
    'buss_022',
    '{"monday": {"open": "12:00", "close": "23:00"},
    "tuesday": {"open": "06:00", "close": "21:00"},
    "wednesday": null,
    "thursday": {"open": "12:00", "close": "17:00"},
    "friday": {"open": "09:00", "close": "16:00"},
    "saturday": {"open": "08:00", "close": "23:00"},
    "sunday": {"open": "09:00", "close": "23:00"}}'
),
(
    'buss_023',
    '{"monday": {"open": "08:00", "close": "21:00"},
    "tuesday": {"open": "09:00", "close": "17:00"},
    "wednesday": null,
    "thursday": {"open": "06:00", "close": "16:00"},
    "friday": null,
    "saturday": {"open": "12:00", "close": "17:00"},
    "sunday": {"open": "08:00", "close": "21:00"}}'
),
(
    'buss_024',
    '{"monday": {"open": "11:00", "close": "17:00"},
    "tuesday": {"open": "12:00", "close": "22:00"},
    "wednesday": null,
    "thursday": {"open": "10:00", "close": "17:00"},
    "friday": {"open": "09:00", "close": "20:00"},
    "saturday": {"open": "12:00", "close": "19:00"},
    "sunday": {"open": "10:00", "close": "18:00"}}'
),
(
    'buss_025',
    '{"monday": {"open": "12:00", "close": "21:00"},
    "tuesday": {"open": "09:00", "close": "19:00"},
    "wednesday": {"open": "08:00", "close": "22:00"},
    "thursday": {"open": "11:00", "close": "16:00"},
    "friday": {"open": "06:00", "close": "18:00"},
    "saturday": {"open": "09:00", "close": "18:00"},
    "sunday": {"open": "07:00", "close": "22:00"}}'
),
(
    'buss_026',
    '{"monday": null,
    "tuesday": {"open": "10:00", "close": "16:00"},
    "wednesday": {"open": "09:00", "close": "18:00"},
    "thursday": {"open": "09:00", "close": "21:00"},
    "friday": {"open": "12:00", "close": "17:00"},
    "saturday": {"open": "09:00", "close": "20:00"},
    "sunday": {"open": "07:00", "close": "19:00"}}'
),
(
    'buss_027',
    '{"monday": {"open": "11:00", "close": "22:00"},
    "tuesday": {"open": "06:00", "close": "20:00"},
    "wednesday": null,
    "thursday": {"open": "10:00", "close": "16:00"},
    "friday": null,
    "saturday": null,
    "sunday": {"open": "09:00", "close": "22:00"}}'
),
(
    'buss_028',
    '{"monday": null,
    "tuesday": null,
    "wednesday": {"open": "07:00", "close": "21:00"},
    "thursday": {"open": "11:00", "close": "18:00"},
    "friday": null,
    "saturday": {"open": "11:00", "close": "20:00"},
    "sunday": {"open": "11:00", "close": "22:00"}}'
),
(
    'buss_029',
    '{"monday": {"open": "08:00", "close": "18:00"},
    "tuesday": {"open": "11:00", "close": "16:00"},
    "wednesday": null,
    "thursday": {"open": "12:00", "close": "18:00"},
    "friday": {"open": "10:00", "close": "18:00"},
    "saturday": null,
    "sunday": {"open": "11:00", "close": "16:00"}}'
),
(
    'buss_030',
    '{"monday": {"open": "07:00", "close": "20:00"},
    "tuesday": {"open": "10:00", "close": "17:00"},
    "wednesday": {"open": "09:00", "close": "22:00"},
    "thursday": null,
    "friday": null,
    "saturday": {"open": "07:00", "close": "22:00"},
    "sunday": {"open": "07:00", "close": "22:00"}}'
);
