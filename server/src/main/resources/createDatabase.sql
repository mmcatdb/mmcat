DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS mapping;
DROP TABLE IF EXISTS logical_model;
DROP TABLE IF EXISTS data_source;
DROP TABLE IF EXISTS database_for_mapping;

DROP TABLE IF EXISTS schema_morphism_in_category;
DROP TABLE IF EXISTS schema_object_in_category;
DROP TABLE IF EXISTS schema_morphism;
DROP TABLE IF EXISTS schema_object;
DROP TABLE IF EXISTS schema_category;

-- Incrementation of the sequnce for generating ids:
-- SELECT nextval('tableName_seq_id')

-- TODO name to label

CREATE TABLE schema_category (
    id SERIAL PRIMARY KEY,
    version VARCHAR(32),
    json_value JSONB NOT NULL
);

INSERT INTO schema_category (version, json_value)
VALUES
    ('0', '{"label": "Article example"}'),
    ('0', '{"label": "Tables to document"}'),
    ('0', '{"label": "Querying example"}');

CREATE TABLE schema_object (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

CREATE TABLE schema_morphism (
    id SERIAL PRIMARY KEY,
    domain_object_id INTEGER NOT NULL REFERENCES schema_object,
    codomain_object_id INTEGER NOT NULL REFERENCES schema_object,
    json_value JSONB NOT NULL
);

CREATE TABLE schema_object_in_category (
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    schema_object_id INTEGER NOT NULL REFERENCES schema_object,
    position JSONB NOT NULL,
    PRIMARY KEY (schema_category_id, schema_object_id)
);

CREATE TABLE schema_morphism_in_category (
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    schema_morphism_id INTEGER NOT NULL REFERENCES schema_morphism,
    PRIMARY KEY (schema_category_id, schema_morphism_id)
);

INSERT INTO schema_object (json_value)
VALUES
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [1], "isNull": false}]}]}, "key": {"value": 1}, "label": "Customer", "superId": {"signatures": [{"ids": [1], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 2}, "label": "Id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [1, 3], "isNull": false}]}]}, "key": {"value": 3}, "label": "Orders", "superId": {"signatures": [{"ids": [1, 3], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [4], "isNull": false}, {"ids": [1, 3, 2], "isNull": false}]}]}, "key": {"value": 4}, "label": "Order", "superId": {"signatures": [{"ids": [4], "isNull": false}, {"ids": [1, 3, 2], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 5}, "label": "Number", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [8, 7], "isNull": false}, {"ids": [6], "isNull": false}]}]}, "key": {"value": 6}, "label": "Contact", "superId": {"signatures": [{"ids": [8, 7], "isNull": false}, {"ids": [6], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 7}, "label": "Value", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [8], "isNull": false}]}]}, "key": {"value": 8}, "label": "Type", "superId": {"signatures": [{"ids": [8], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 9}, "label": "Name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [1, 3, 2, -9], "isNull": false}, {"ids": [4, -9], "isNull": false}, {"ids": [12, 10], "isNull": false}]}]}, "key": {"value": 10}, "label": "Items", "superId": {"signatures": [{"ids": [1, 3, 2, -9], "isNull": false}, {"ids": [4, -9], "isNull": false}, {"ids": [12, 10], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 11}, "label": "Quantity", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [12], "isNull": false}]}]}, "key": {"value": 12}, "label": "Product", "superId": {"signatures": [{"ids": [12], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 13}, "label": "Id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 14}, "label": "Name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 15}, "label": "Price", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [4], "isNull": false}]}]}, "key": {"value": 1}, "label": "customer", "superId": {"signatures": [{"ids": [4], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 2}, "label": "full name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 3}, "label": "id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [1], "isNull": false}]}]}, "key": {"value": 4}, "label": "contact", "superId": {"signatures": [{"ids": [1], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 5}, "label": "type", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 6}, "label": "value", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 7}, "label": "id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [4, 7], "isNull": false}, {"ids": [1, 6], "isNull": false}]}]}, "key": {"value": 8}, "label": "customer contact", "superId": {"signatures": [{"ids": [4, 7], "isNull": false}, {"ids": [1, 6], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [13], "isNull": false}]}]}, "key": {"value": 9}, "label": "order", "superId": {"signatures": [{"ids": [13], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 10}, "label": "created", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 11}, "label": "paid", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 12}, "label": "sent", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 13}, "label": "note", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 14}, "label": "delivery address", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 15}, "label": "id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [15], "isNull": false}]}]}, "key": {"value": 16}, "label": "product", "superId": {"signatures": [{"ids": [15], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 17}, "label": "price", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 18}, "label": "name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 19}, "label": "id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [13, 21], "isNull": false}, {"ids": [15, 20], "isNull": false}]}]}, "key": {"value": 20}, "label": "order item", "superId": {"signatures": [{"ids": [13, 21], "isNull": false}, {"ids": [15, 20], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 21}, "label": "amount", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 22}, "label": "total price", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [2], "isNull": false}]}]}, "key": {"value": 1}, "label": "Customer", "superId": {"signatures": [{"ids": [2], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 2}, "label": "Name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 3}, "label": "Id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 4}, "label": "Surname", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [2, 4], "isNull": false}, {"ids": [2, 5], "isNull": false}]}]}, "key": {"value": 5}, "label": "Friends", "superId": {"signatures": [{"ids": [2, 4], "isNull": false}, {"ids": [2, 5], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [7], "isNull": false}, {"ids": [8], "isNull": false}, {"ids": [2, 6], "isNull": false}]}]}, "key": {"value": 6}, "label": "Contact", "superId": {"signatures": [{"ids": [7], "isNull": false}, {"ids": [8], "isNull": false}, {"ids": [2, 6], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 7}, "label": "Key", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 8}, "label": "Value", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [2, 9], "isNull": false}, {"ids": [11, 10], "isNull": false}]}]}, "key": {"value": 9}, "label": "Orders", "superId": {"signatures": [{"ids": [2, 9], "isNull": false}, {"ids": [11, 10], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [11], "isNull": false}]}]}, "key": {"value": 10}, "label": "Order", "superId": {"signatures": [{"ids": [11], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 11}, "label": "Id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [11, 12], "isNull": false}, {"ids": [14, 13], "isNull": false}]}]}, "key": {"value": 12}, "label": "Items", "superId": {"signatures": [{"ids": [11, 12], "isNull": false}, {"ids": [14, 13], "isNull": false}]}}'),
    ('{"ids": {"type": "Signatures", "signatureIds": [{"signatures": [{"ids": [14], "isNull": false}]}]}, "key": {"value": 13}, "label": "Product", "superId": {"signatures": [{"ids": [14], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 14}, "label": "Number", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 15}, "label": "Name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": {"type": "Value", "signatureIds": null}, "key": {"value": 16}, "label": "Price", "superId": {"signatures": [{"ids": [], "isNull": false}]}}');

INSERT INTO schema_object_in_category (schema_category_id, schema_object_id, position)
VALUES
    (1, 1, '{"x": -99, "y": -5}'),
    (1, 2, '{"x": -138, "y": 94}'),
    (1, 3, '{"x": -11, "y": -75}'),
    (1, 4, '{"x": 134, "y": -85}'),
    (1, 5, '{"x": 140, "y": -188}'),
    (1, 6, '{"x": 271, "y": -83}'),
    (1, 7, '{"x": 273, "y": -190}'),
    (1, 8, '{"x": 394, "y": -86}'),
    (1, 9, '{"x": 399, "y": -180}'),
    (1, 10, '{"x": 136, "y": -6}'),
    (1, 11, '{"x": 258, "y": -5}'),
    (1, 12, '{"x": 128, "y": 85}'),
    (1, 13, '{"x": 47, "y": 189}'),
    (1, 14, '{"x": 125, "y": 187}'),
    (1, 15, '{"x": 213, "y": 189}'),
    (2, 16, '{"x": -21, "y": 135}'),
    (2, 17, '{"x": -94, "y": 287}'),
    (2, 18, '{"x": 48, "y": 286}'),
    (2, 19, '{"x": -250, "y": 109}'),
    (2, 20, '{"x": -415, "y": 54}'),
    (2, 21, '{"x": -403, "y": 152}'),
    (2, 22, '{"x": -344, "y": 233}'),
    (2, 23, '{"x": -172, "y": 194}'),
    (2, 24, '{"x": 191, "y": 129}'),
    (2, 25, '{"x": 334, "y": 232}'),
    (2, 26, '{"x": 175, "y": 286}'),
    (2, 27, '{"x": 269, "y": 295}'),
    (2, 28, '{"x": 347, "y": 127}'),
    (2, 29, '{"x": 338, "y": 30}'),
    (2, 30, '{"x": 251, "y": -23}'),
    (2, 31, '{"x": -79, "y": -64}'),
    (2, 32, '{"x": -187, "y": 5}'),
    (2, 33, '{"x": -221, "y": -96}'),
    (2, 34, '{"x": -148, "y": -177}'),
    (2, 35, '{"x": 94, "y": -22}'),
    (2, 36, '{"x": 49, "y": -150}'),
    (2, 37, '{"x": 166, "y": -145}'),
    (3, 49, '{"x": 561, "y": 415}'),
    (3, 50, '{"x": 556, "y": 532}'),
    (3, 51, '{"x": 552, "y": 178}'),
    (3, 52, '{"x": 717, "y": 405}'),
    (3, 53, '{"x": 722, "y": 525}'),
    (3, 38, '{"x": 293, "y": 307}'),
    (3, 39, '{"x": 147, "y": 308}'),
    (3, 40, '{"x": 294, "y": 190}'),
    (3, 41, '{"x": 141, "y": 404}'),
    (3, 42, '{"x": 155, "y": 206}'),
    (3, 43, '{"x": 189, "y": 520}'),
    (3, 44, '{"x": 189, "y": 656}'),
    (3, 45, '{"x": 300, "y": 657}'),
    (3, 46, '{"x": 433, "y": 304}'),
    (3, 47, '{"x": 559, "y": 299}'),
    (3, 48, '{"x": 439, "y": 533}');

INSERT INTO schema_morphism (domain_object_id, codomain_object_id, json_value)
VALUES
    (1, 2, '{"max": "ONE", "min": "ONE", "signature": {"ids": [1], "isNull": false}}'),
    (2, 1, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-1], "isNull": false}}'),
    (4, 3, '{"max": "ONE", "min": "ONE", "signature": {"ids": [2], "isNull": false}}'),
    (3, 4, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-2], "isNull": false}}'),
    (3, 1, '{"max": "ONE", "min": "ONE", "signature": {"ids": [3], "isNull": false}}'),
    (1, 3, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-3], "isNull": false}}'),
    (4, 5, '{"max": "ONE", "min": "ONE", "signature": {"ids": [4], "isNull": false}}'),
    (5, 4, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-4], "isNull": false}}'),
    (4, 6, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [5], "isNull": false}}'),
    (6, 4, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-5], "isNull": false}}'),
    (6, 7, '{"max": "ONE", "min": "ONE", "signature": {"ids": [6], "isNull": false}}'),
    (7, 6, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-6], "isNull": false}}'),
    (6, 8, '{"max": "ONE", "min": "ONE", "signature": {"ids": [7], "isNull": false}}'),
    (8, 6, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-7], "isNull": false}}'),
    (8, 9, '{"max": "ONE", "min": "ONE", "signature": {"ids": [8], "isNull": false}}'),
    (9, 8, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-8], "isNull": false}}'),
    (4, 10, '{"max": "STAR", "min": "ONE", "signature": {"ids": [9], "isNull": false}}'),
    (10, 4, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-9], "isNull": false}}'),
    (10, 12, '{"max": "ONE", "min": "ONE", "signature": {"ids": [10], "isNull": false}}'),
    (12, 10, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-10], "isNull": false}}'),
    (10, 11, '{"max": "ONE", "min": "ZERO", "signature": {"ids": [11], "isNull": false}}'),
    (11, 10, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-11], "isNull": false}}'),
    (12, 13, '{"max": "ONE", "min": "ONE", "signature": {"ids": [12], "isNull": false}}'),
    (13, 12, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-12], "isNull": false}}'),
    (12, 14, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [13], "isNull": false}}'),
    (14, 12, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-13], "isNull": false}}'),
    (12, 15, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [14], "isNull": false}}'),
    (15, 12, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-14], "isNull": false}}'),
    (19, 22, '{"max": "ONE", "min": "ONE", "signature": {"ids": [1], "isNull": false}}'),
    (22, 19, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-1], "isNull": false}}'),
    (19, 21, '{"max": "ONE", "min": "ONE", "signature": {"ids": [2], "isNull": false}}'),
    (21, 19, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-2], "isNull": false}}'),
    (19, 20, '{"max": "ONE", "min": "ONE", "signature": {"ids": [3], "isNull": false}}'),
    (20, 19, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-3], "isNull": false}}'),
    (16, 18, '{"max": "ONE", "min": "ONE", "signature": {"ids": [4], "isNull": false}}'),
    (18, 16, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-4], "isNull": false}}'),
    (16, 17, '{"max": "ONE", "min": "ONE", "signature": {"ids": [5], "isNull": false}}'),
    (17, 16, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-5], "isNull": false}}'),
    (23, 19, '{"max": "ONE", "min": "ONE", "signature": {"ids": [6], "isNull": false}}'),
    (19, 23, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-6], "isNull": false}}'),
    (23, 16, '{"max": "ONE", "min": "ONE", "signature": {"ids": [7], "isNull": false}}'),
    (16, 23, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-7], "isNull": false}}'),
    (24, 25, '{"max": "ONE", "min": "ZERO", "signature": {"ids": [8], "isNull": false}}'),
    (25, 24, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-8], "isNull": false}}'),
    (24, 27, '{"max": "ONE", "min": "ZERO", "signature": {"ids": [9], "isNull": false}}'),
    (27, 24, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-9], "isNull": false}}'),
    (24, 26, '{"max": "ONE", "min": "ZERO", "signature": {"ids": [10], "isNull": false}}'),
    (26, 24, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-10], "isNull": false}}'),
    (24, 28, '{"max": "ONE", "min": "ONE", "signature": {"ids": [11], "isNull": false}}'),
    (28, 24, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-11], "isNull": false}}'),
    (24, 29, '{"max": "ONE", "min": "ONE", "signature": {"ids": [12], "isNull": false}}'),
    (29, 24, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-12], "isNull": false}}'),
    (24, 30, '{"max": "ONE", "min": "ONE", "signature": {"ids": [13], "isNull": false}}'),
    (30, 24, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-13], "isNull": false}}'),
    (24, 16, '{"max": "ONE", "min": "ONE", "signature": {"ids": [14], "isNull": false}}'),
    (16, 24, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-14], "isNull": false}}'),
    (31, 34, '{"max": "ONE", "min": "ONE", "signature": {"ids": [15], "isNull": false}}'),
    (34, 31, '{"max": "ONE", "min": "ONE", "signature": {"ids": [-15], "isNull": false}}'),
    (31, 33, '{"max": "ONE", "min": "ONE", "signature": {"ids": [16], "isNull": false}}'),
    (33, 31, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-16], "isNull": false}}'),
    (31, 32, '{"max": "ONE", "min": "ONE", "signature": {"ids": [17], "isNull": false}}'),
    (32, 31, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-17], "isNull": false}}'),
    (35, 37, '{"max": "ONE", "min": "ONE", "signature": {"ids": [18], "isNull": false}}'),
    (37, 35, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-18], "isNull": false}}'),
    (35, 36, '{"max": "ONE", "min": "ONE", "signature": {"ids": [19], "isNull": false}}'),
    (36, 35, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-19], "isNull": false}}'),
    (35, 31, '{"max": "ONE", "min": "ONE", "signature": {"ids": [20], "isNull": false}}'),
    (31, 35, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-20], "isNull": false}}'),
    (35, 24, '{"max": "ONE", "min": "ONE", "signature": {"ids": [21], "isNull": false}}'),
    (24, 35, '{"max": "STAR", "min": "ONE", "signature": {"ids": [-21], "isNull": false}}'),
    (38, 39, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [1], "isNull": false}}'),
    (39, 38, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-1], "isNull": false}}'),
    (38, 40, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [2], "isNull": false}}'),
    (40, 38, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [-2], "isNull": false}}'),
    (38, 41, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [3], "isNull": false}}'),
    (41, 38, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-3], "isNull": false}}'),
    (42, 38, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [4], "isNull": false}}'),
    (38, 42, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-4], "isNull": false}}'),
    (42, 38, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [5], "isNull": false}}'),
    (38, 42, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-5], "isNull": false}}'),
    (43, 38, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [6], "isNull": false}}'),
    (38, 43, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-6], "isNull": false}}'),
    (43, 44, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [7], "isNull": false}}'),
    (44, 43, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-7], "isNull": false}}'),
    (43, 45, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [8], "isNull": false}}'),
    (45, 43, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-8], "isNull": false}}'),
    (46, 38, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [9], "isNull": false}}'),
    (38, 46, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-9], "isNull": false}}'),
    (46, 47, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [10], "isNull": false}}'),
    (47, 46, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-10], "isNull": false}}'),
    (47, 51, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [11], "isNull": false}}'),
    (51, 47, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [-11], "isNull": false}}'),
    (49, 47, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [12], "isNull": false}}'),
    (47, 49, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-12], "isNull": false}}'),
    (49, 50, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [13], "isNull": false}}'),
    (50, 49, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-13], "isNull": false}}'),
    (50, 48, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [14], "isNull": false}}'),
    (48, 50, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [-14], "isNull": false}}'),
    (50, 52, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [15], "isNull": false}}'),
    (52, 50, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-15], "isNull": false}}'),
    (50, 53, '{"max": "ONE", "min": "ONE", "label": "", "signature": {"ids": [16], "isNull": false}}'),
    (53, 50, '{"max": "STAR", "min": "ZERO", "label": "", "signature": {"ids": [-16], "isNull": false}}');

INSERT INTO schema_morphism_in_category (schema_category_id, schema_morphism_id)
VALUES
    (1, 1),
    (1, 2),
    (1, 3),
    (1, 4),
    (1, 5),
    (1, 6),
    (1, 7),
    (1, 8),
    (1, 9),
    (1, 10),
    (1, 11),
    (1, 12),
    (1, 13),
    (1, 14),
    (1, 15),
    (1, 16),
    (1, 17),
    (1, 18),
    (1, 19),
    (1, 20),
    (1, 21),
    (1, 22),
    (1, 23),
    (1, 24),
    (1, 25),
    (1, 26),
    (1, 27),
    (1, 28),
    (2, 29),
    (2, 30),
    (2, 31),
    (2, 32),
    (2, 33),
    (2, 34),
    (2, 35),
    (2, 36),
    (2, 37),
    (2, 38),
    (2, 39),
    (2, 40),
    (2, 41),
    (2, 42),
    (2, 43),
    (2, 44),
    (2, 45),
    (2, 46),
    (2, 47),
    (2, 48),
    (2, 49),
    (2, 50),
    (2, 51),
    (2, 52),
    (2, 53),
    (2, 54),
    (2, 55),
    (2, 56),
    (2, 57),
    (2, 58),
    (2, 59),
    (2, 60),
    (2, 61),
    (2, 62),
    (2, 63),
    (2, 64),
    (2, 65),
    (2, 66),
    (2, 67),
    (2, 68),
    (2, 69),
    (2, 70),
    (3, 71),
    (3, 72),
    (3, 73),
    (3, 74),
    (3, 75),
    (3, 76),
    (3, 77),
    (3, 78),
    (3, 79),
    (3, 80),
    (3, 81),
    (3, 82),
    (3, 83),
    (3, 84),
    (3, 85),
    (3, 86),
    (3, 87),
    (3, 88),
    (3, 89),
    (3, 90),
    (3, 91),
    (3, 92),
    (3, 93),
    (3, 94),
    (3, 95),
    (3, 96),
    (3, 97),
    (3, 98),
    (3, 99),
    (3, 100),
    (3, 101),
    (3, 102);

CREATE TABLE database_for_mapping (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO database_for_mapping (json_value)
VALUES
    ('{ "type": "mongodb", "label": "MongoDB",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mmcat_server_data",
            "authenticationDatabase": "admin",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mmcat_server_data",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL TTD",
        "settings": {
            "host": "localhost",
            "port": 5432,
            "database": "mmcat_server_ttd",
            "password": "mmcat_password",
            "username": "mmcat_user"
        }
    }'),
    ('{ "type": "mongodb", "label": "MongoDB Experiments",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mmcat_server_experiments",
            "authenticationDatabase": "admin",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL Experiments",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mmcat_server_experiments",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{ "type": "mongodb", "label": "MongoDB - Querying",
        "settings": {
            "host": "localhost",
            "port": 27017,
            "database": "mmcat_server_querying",
            "password": "mmcat_password",
            "username": "mmcat_user",
            "authenticationDatabase": "admin"
        }
    }'),
    ('{ "type": "postgresql", "label": "PostgreSQL - Querying",
        "settings": {
            "host": "localhost",
            "port": 5432,
            "database": "mmcat_server_querying",
            "password": "mmcat_password",
            "username": "mmcat_user",
            "authenticationDatabase": ""
        }
    }'),
    ('{ "type": "postgresql", "label": "Neo4j - Querying",
        "settings": {
            "host": "localhost",
            "port": 5432,
            "database": "mmcat_server_querying",
            "password": "mmcat_password",
            "username": "mmcat_user",
            "authenticationDatabase": ""
        }
    }');

CREATE TABLE data_source (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO data_source (json_value)
VALUES
    ('{
        "url": "http://nosql.ms.mff.cuni.cz/mmcat/data-sources/test2.jsonld",
        "label": "test2",
        "type": "JsonLdStore"
    }');

CREATE TABLE logical_model (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    database_id INTEGER NOT NULL REFERENCES database_for_mapping,
    json_value JSONB NOT NULL
);

INSERT INTO logical_model (schema_category_id, database_id, json_value)
VALUES
    (1, 1, '{"label": "Mongo - Order"}'),
    (1, 2, '{"label": "Postgres - Customer"}'),
    (2, 3, '{"label": "Postgres import"}'),
    (2, 1, '{"label": "Mongo export"}'),
    (3, 6, '{"label": "MongoDB"}'),
    (3, 7, '{"label": "PostgreSQL"}'),
    (3, 8, '{"label": "Neo4j"}');

CREATE TABLE mapping (
    id SERIAL PRIMARY KEY,
    logical_model_id INTEGER NOT NULL REFERENCES logical_model,
    root_object_id INTEGER NOT NULL REFERENCES schema_object,
    mapping_json_value JSONB NOT NULL,
    json_value JSONB NOT NULL
);

-- databázový systém může obsahovat více databázových instancí
    -- - v jedné db instanci musí být jména kindů atd unikátní

INSERT INTO mapping (logical_model_id, root_object_id, mapping_json_value, json_value)
VALUES
(1, 4, '{"primaryKey": [{"ids": [4], "isNull": false}, {"ids": [1, 3, 2], "isNull": false}], "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "Order", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "_id", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"type": "STATIC_NAME", "value": "customer", "_class": "StaticName"}, "value": {"signature": {"ids": [1, 3, 2], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "number", "_class": "StaticName"}, "value": {"signature": {"ids": [4], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
                }, {
                    "name": {"type": "STATIC_NAME", "value": "contact", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"_class": "DynamicName", "signature": {"ids": [8, 7], "isNull": false}}, "value": {"signature": {"ids": [6], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [5], "isNull": false}, "isAuxiliary": false
                }, {
                    "name": {"type": "STATIC_NAME", "value": "items", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [12, 10], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"signature": {"ids": [13, 10], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "price", "_class": "StaticName"}, "value": {"signature": {"ids": [14, 10], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "quantity", "_class": "StaticName"}, "value": {"signature": {"ids": [11], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [9], "isNull": false}, "isAuxiliary": false
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "Order"}'
    ),
    (2, 1, '{"primaryKey": [{"ids": [1], "isNull": false}], "kindName": "customer",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "Customer", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [1], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "Customer"}'
    ),
    (3, 16, '{"primaryKey": [{"ids": [4], "isNull": false}], "kindName": "app_customer",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "customer", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [4], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "full_name", "_class": "StaticName"}, "value": {"signature": {"ids": [5], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "CustomerTable"}'
    ),
    (3, 19, '{"primaryKey": [{"ids": [1], "isNull": false}], "kindName": "app_contact",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "contact", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [1], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "value", "_class": "StaticName"}, "value": {"signature": {"ids": [2], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "type", "_class": "StaticName"}, "value": {"signature": {"ids": [3], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "ContactTable"}'
    ),
    (3, 23, '{"primaryKey": [{"ids": [4, 7], "isNull": false}, {"ids": [1, 6], "isNull": false}], "kindName": "app_customer_contact",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "customer_contact", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "customer_id", "_class": "StaticName"}, "value": {"signature": {"ids": [4, 7], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "contact_id", "_class": "StaticName"}, "value": {"signature": {"ids": [1, 6], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "CustomerContactTable"}'
    ),
    (3, 24, '{"primaryKey": [{"ids": [13], "isNull": false}], "kindName": "app_order",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "order", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [13], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "delivery_address", "_class": "StaticName"}, "value": {"signature": {"ids": [12], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "note", "_class": "StaticName"}, "value": {"signature": {"ids": [11], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "created", "_class": "StaticName"}, "value": {"signature": {"ids": [8], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "sent", "_class": "StaticName"}, "value": {"signature": {"ids": [9], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "paid", "_class": "StaticName"}, "value": {"signature": {"ids": [10], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "customer_id", "_class": "StaticName"}, "value": {"signature": {"ids": [4, 14], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "OrderTable"}'
    ),
    (3, 31, '{"primaryKey": [{"ids": [15], "isNull": false}], "kindName": "app_product",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "product", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [15], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"signature": {"ids": [16], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "price", "_class": "StaticName"}, "value": {"signature": {"ids": [17], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "ProductTable"}'
    ),
    (3, 35, '{"primaryKey": [{"ids": [13, 21], "isNull": false}, {"ids": [15, 20], "isNull": false}], "kindName": "app_order_item",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "order_item", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "order_id", "_class": "StaticName"}, "value": {"signature": {"ids": [13, 21], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "product_id", "_class": "StaticName"}, "value": {"signature": {"ids": [15, 20], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "amount", "_class": "StaticName"}, "value": {"signature": {"ids": [19], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "total_price", "_class": "StaticName"}, "value": {"signature": {"ids": [18], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "OrderItemTable"}'
    ),
    (4, 24, '{"primaryKey": [{"ids": [13], "isNull": false}], "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "order", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "customer", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"type": "STATIC_NAME", "value": "contact", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                                {
                                    "name": {"_class": "DynamicName", "signature": {"ids": [3], "isNull": false}}, "value": {"signature": {"ids": [2], "isNull": false}}, "_class": "SimpleProperty"
                                }
                            ], "signature": {"ids": [6, -7], "isNull": false}, "isAuxiliary": false
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"signature": {"ids": [5], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [14], "isNull": false}, "isAuxiliary": false
                }, {
                    "name": {"type": "STATIC_NAME", "value": "address", "_class": "StaticName"}, "value": {"signature": {"ids": [12], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "note", "_class": "StaticName"}, "value": {"signature": {"ids": [11], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "events", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"type": "STATIC_NAME", "value": "created", "_class": "StaticName"}, "value": {"signature": {"ids": [8], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "sent", "_class": "StaticName"}, "value": {"signature": {"ids": [9], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "paid", "_class": "StaticName"}, "value": {"signature": {"ids": [10], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
                }, {
                    "name": {"type": "STATIC_NAME", "value": "items", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"type": "STATIC_NAME", "value": "amount", "_class": "StaticName"}, "value": {"signature": {"ids": [19], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "total_price", "_class": "StaticName"}, "value": {"signature": {"ids": [18], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"signature": {"ids": [16, 20], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [-21], "isNull": false}, "isAuxiliary": false
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "OrderDocument"}'
    ),
    (5, 47, '{"primaryKey": [{"ids": [11], "isNull": false}], "kindName": "order",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "order", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "number", "_class": "StaticName"}, "value": {"signature": {"ids": [11], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "customers", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [2], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [9, -10], "isNull": false}, "isAuxiliary": false
                }, {
                    "name": {"type": "STATIC_NAME", "value": "items", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                        {
                            "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [14, 13], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"signature": {"ids": [15, 13], "isNull": false}}, "_class": "SimpleProperty"
                        }, {
                            "name": {"type": "STATIC_NAME", "value": "price", "_class": "StaticName"}, "value": {"signature": {"ids": [16, 13], "isNull": false}}, "_class": "SimpleProperty"
                        }
                    ], "signature": {"ids": [-12], "isNull": false}, "isAuxiliary": false
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "Order"}'
    ),
    (6, 38, '{"primaryKey": [{"ids": [2], "isNull": false}], "kindName": "customer",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "customer", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [2], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"signature": {"ids": [1], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "surname", "_class": "StaticName"}, "value": {"signature": {"ids": [3], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "Customer"}'
    ),
    (7, 42, '{"primaryKey": [{"ids": [2, 4], "isNull": false}, {"ids": [2, 5], "isNull": false}], "kindName": "friends",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "friends", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "first_customer_id", "_class": "StaticName"}, "value": {"signature": {"ids": [2, 4], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "second_customer_id", "_class": "StaticName"}, "value": {"signature": {"ids": [2, 5], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "Friends"}'
    ),
    (7, 43, '{"primaryKey": [{"ids": [7], "isNull": false}, {"ids": [8], "isNull": false}, {"ids": [2, 6], "isNull": false}], "kindName": "contact",
        "accessPath": {
            "name": {"type": "STATIC_NAME", "value": "contact", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [
                {
                    "name": {"type": "STATIC_NAME", "value": "key", "_class": "StaticName"}, "value": {"signature": {"ids": [7], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "value", "_class": "StaticName"}, "value": {"signature": {"ids": [8], "isNull": false}}, "_class": "SimpleProperty"
                }, {
                    "name": {"type": "STATIC_NAME", "value": "customer_id", "_class": "StaticName"}, "value": {"signature": {"ids": [2, 6], "isNull": false}}, "_class": "SimpleProperty"
                }
            ], "signature": {"ids": [0], "isNull": true}, "isAuxiliary": true
        }}',
        '{"label": "Contacts"}'
    );
    
CREATE TABLE job (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    logical_model_id INTEGER REFERENCES logical_model,
    data_source_id INTEGER REFERENCES database_for_mapping, -- TODO make job to contain either logical_model_id or data_source_id
    json_value JSONB NOT NULL
    -- přidat typ jobu, vstup, výstup, vše serializované v jsonu
        -- podobně jako ukládání logování
        -- součástí log4j je nastavení kam se to dá ukládat, resp. do libovolné kombinace uložišť
            -- např. prometheus, zabbix, kibana - monitorování stavu aplikace

);

INSERT INTO job (schema_category_id, logical_model_id, data_source_id, json_value)
VALUES
    (1, 1, null, '{"label": "Import Order", "type": "ModelToCategory", "status": "Ready"}'),
    (1, 1, null, '{"label": "Export Order", "type": "CategoryToModel", "status": "Ready"}'),
    (1, 2, null, '{"label": "Import Customer", "type": "ModelToCategory", "status": "Ready"}'),
    (1, 2, null, '{"label": "Export Customer", "type": "CategoryToModel", "status": "Ready"}'),
    (2, 3, null, '{"label": "Import from Postgres", "type": "ModelToCategory", "status": "Ready"}'),
    (2, 4, null, '{"label": "Export to Mongo", "type": "CategoryToModel", "status": "Ready"}');
