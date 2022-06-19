DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS mapping;
DROP TABLE IF EXISTS database_for_mapping;

DROP TABLE IF EXISTS schema_morphism_in_category;
DROP TABLE IF EXISTS schema_object_in_category;
DROP TABLE IF EXISTS schema_morphism;
DROP TABLE IF EXISTS schema_object;
DROP TABLE IF EXISTS schema_category;


CREATE TABLE schema_category (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO schema_category (json_value)
VALUES ('{ "name": "test schema category" }');

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
    ('{"ids": [{"signatures": [{"ids": [1], "isNull": false}]}], "key": {"value": 1}, "label": "Customer", "superId": {"signatures": [{"ids": [1], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 2}, "label": "Id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [1, 3], "isNull": false}]}], "key": {"value": 3}, "label": "Orders", "superId": {"signatures": [{"ids": [1, 3], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [4], "isNull": false}, {"ids": [1, 3, 2], "isNull": false}]}], "key": {"value": 4}, "label": "Order", "superId": {"signatures": [{"ids": [4], "isNull": false}, {"ids": [1, 3, 2], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 5}, "label": "Number", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [8, 7], "isNull": false}, {"ids": [6], "isNull": false}]}], "key": {"value": 6}, "label": "Contact", "superId": {"signatures": [{"ids": [8, 7], "isNull": false}, {"ids": [6], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 7}, "label": "Value", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [8], "isNull": false}]}], "key": {"value": 8}, "label": "Type", "superId": {"signatures": [{"ids": [8], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 9}, "label": "Name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [1, 3, 2, -9], "isNull": false}, {"ids": [4, -9], "isNull": false}, {"ids": [12, 10], "isNull": false}]}], "key": {"value": 10}, "label": "Items", "superId": {"signatures": [{"ids": [1, 3, 2, -9], "isNull": false}, {"ids": [4, -9], "isNull": false}, {"ids": [12, 10], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 11}, "label": "Quantity", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [12], "isNull": false}]}], "key": {"value": 12}, "label": "Product", "superId": {"signatures": [{"ids": [12], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 13}, "label": "Id", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 14}, "label": "Name", "superId": {"signatures": [{"ids": [], "isNull": false}]}}'),
    ('{"ids": [{"signatures": [{"ids": [], "isNull": false}]}], "key": {"value": 15}, "label": "Price", "superId": {"signatures": [{"ids": [], "isNull": false}]}}');

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
    (1, 15, '{"x": 213, "y": 189}');

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
    (15, 12, '{"max": "STAR", "min": "ZERO", "signature": {"ids": [-14], "isNull": false}}');

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
    (1, 28);

CREATE TABLE database_for_mapping (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO database_for_mapping (json_value)
VALUES
    ('{"type":"mongodb","label":"MongoDB",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mmcat_server_data",
            "authenticationDatabase": "admin",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{"type":"postgresql","label":"PostgreSQL",
        "settings": {
            "host": "localhost",
            "port": "5432",
            "database": "mmcat_server_data",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }');

CREATE TABLE mapping (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    database_id INTEGER NOT NULL REFERENCES database_for_mapping,
    root_object_id INTEGER REFERENCES schema_object,
    root_morphism_id INTEGER REFERENCES schema_morphism,
    mapping_json_value JSONB NOT NULL,
    json_value JSONB NOT NULL
);

-- databázový systém může obsahovat více databázových instancí
    -- - v jedné db instanci musí být jména kindů atd unikátní


INSERT INTO mapping (schema_category_id, database_id, root_object_id, root_morphism_id, mapping_json_value, json_value)
VALUES
    (1, 1, 4, NULL, '{"pkey": [], "kindName": "order", "accessPath": {"name": {"type": "STATIC_NAME", "value": "Order", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "_id", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "customer", "_class": "StaticName"}, "value": {"signature": {"ids": [1, 3, 2], "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "number", "_class": "StaticName"}, "value": {"signature": {"ids": [4], "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [0], "isNull": true}}, {"name": {"type": "STATIC_NAME", "value": "contact", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"_class": "DynamicName", "signature": {"ids": [8, 7], "isNull": false}}, "value": {"signature": {"ids": [6], "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [5], "isNull": false}}, {"name": {"type": "STATIC_NAME", "value": "items", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [12, 10], "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"signature": {"ids": [13, 10], "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "price", "_class": "StaticName"}, "value": {"signature": {"ids": [14, 10], "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "quantity", "_class": "StaticName"}, "value": {"signature": {"ids": [11], "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [9], "isNull": false}}], "signature": {"ids": [0], "isNull": true}}}', '{"name": "Order"}'),
    (1, 2, 1, NULL, '{"pkey": [], "kindName": "customer", "accessPath": {"name": {"type": "STATIC_NAME", "value": "Customer", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"signature": {"ids": [1], "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [0], "isNull": true}}}', '{"name": "Customer"}');

CREATE TABLE job (
    id SERIAL PRIMARY KEY,
    mapping_id INTEGER NOT NULL REFERENCES mapping,
    json_value JSONB NOT NULL
    -- přidat typ jobu, vstup, výstup, vše serializované v jsonu
        -- podobně jako ukládání logování
        -- součástí log4j je nastavení kam se to dá ukládat, resp. do libovolné kombinace uložišť
            -- např. prometheus, zabbix, kibana - monitorování stavu aplikace

);

INSERT INTO job (mapping_id, json_value)
VALUES
    (1, '{"name": "Import Order", "type": "ModelToCategory", "status": "Canceled"}');

