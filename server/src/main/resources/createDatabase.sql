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
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [1], "_class": "Signature", "isNull": false}]}], "key": {"value": 1, "_class": "Key"}, "label": "Customer", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [1], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 2, "_class": "Key"}, "label": "Id", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [1, 3], "_class": "Signature", "isNull": false}]}], "key": {"value": 3, "_class": "Key"}, "label": "Orders", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [1, 3], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [4], "_class": "Signature", "isNull": false}, {"ids": [1, 3, 2], "_class": "Signature", "isNull": false}]}], "key": {"value": 4, "_class": "Key"}, "label": "Order", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [4], "_class": "Signature", "isNull": false}, {"ids": [1, 3, 2], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 5, "_class": "Key"}, "label": "Number", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [8, 7], "_class": "Signature", "isNull": false}, {"ids": [6], "_class": "Signature", "isNull": false}]}], "key": {"value": 6, "_class": "Key"}, "label": "Contact", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [8, 7], "_class": "Signature", "isNull": false}, {"ids": [6], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 7, "_class": "Key"}, "label": "Value", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [8], "_class": "Signature", "isNull": false}]}], "key": {"value": 8, "_class": "Key"}, "label": "Type", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [8], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 9, "_class": "Key"}, "label": "Name", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [1, 3, 2, -9], "_class": "Signature", "isNull": false}, {"ids": [4, -9], "_class": "Signature", "isNull": false}, {"ids": [12, 10], "_class": "Signature", "isNull": false}]}], "key": {"value": 10, "_class": "Key"}, "label": "Items", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [1, 3, 2, -9], "_class": "Signature", "isNull": false}, {"ids": [4, -9], "_class": "Signature", "isNull": false}, {"ids": [12, 10], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 11, "_class": "Key"}, "label": "Quantity", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [12], "_class": "Signature", "isNull": false}]}], "key": {"value": 12, "_class": "Key"}, "label": "Product", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [12], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 13, "_class": "Key"}, "label": "Id", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 14, "_class": "Key"}, "label": "Name", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 15, "_class": "Key"}, "label": "Price", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}}');

INSERT INTO schema_object_in_category (schema_category_id, schema_object_id, position)
VALUES
    (1, 1, '{"x": -99, "y": -5, "_class": "Position"}'),
    (1, 2, '{"x": -138, "y": 94, "_class": "Position"}'),
    (1, 3, '{"x": -11, "y": -75, "_class": "Position"}'),
    (1, 4, '{"x": 134, "y": -85, "_class": "Position"}'),
    (1, 5, '{"x": 140, "y": -188, "_class": "Position"}'),
    (1, 6, '{"x": 271, "y": -83, "_class": "Position"}'),
    (1, 7, '{"x": 273, "y": -190, "_class": "Position"}'),
    (1, 8, '{"x": 394, "y": -86, "_class": "Position"}'),
    (1, 9, '{"x": 399, "y": -180, "_class": "Position"}'),
    (1, 10, '{"x": 136, "y": -6, "_class": "Position"}'),
    (1, 11, '{"x": 258, "y": -5, "_class": "Position"}'),
    (1, 12, '{"x": 128, "y": 85, "_class": "Position"}'),
    (1, 13, '{"x": 47, "y": 189, "_class": "Position"}'),
    (1, 14, '{"x": 125, "y": 187, "_class": "Position"}'),
    (1, 15, '{"x": 213, "y": 189, "_class": "Position"}');

INSERT INTO schema_morphism (domain_object_id, codomain_object_id, json_value)
VALUES
    (1, 2, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [1], "_class": "Signature", "isNull": false}}'),
    (2, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-1], "_class": "Signature", "isNull": false}}'),
    (4, 3, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [2], "_class": "Signature", "isNull": false}}'),
    (3, 4, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-2], "_class": "Signature", "isNull": false}}'),
    (3, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [3], "_class": "Signature", "isNull": false}}'),
    (1, 3, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-3], "_class": "Signature", "isNull": false}}'),
    (4, 5, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [4], "_class": "Signature", "isNull": false}}'),
    (5, 4, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-4], "_class": "Signature", "isNull": false}}'),
    (4, 6, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [5], "_class": "Signature", "isNull": false}}'),
    (6, 4, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-5], "_class": "Signature", "isNull": false}}'),
    (6, 7, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [6], "_class": "Signature", "isNull": false}}'),
    (7, 6, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-6], "_class": "Signature", "isNull": false}}'),
    (6, 8, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [7], "_class": "Signature", "isNull": false}}'),
    (8, 6, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-7], "_class": "Signature", "isNull": false}}'),
    (8, 9, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [8], "_class": "Signature", "isNull": false}}'),
    (9, 8, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-8], "_class": "Signature", "isNull": false}}'),
    (4, 10, '{"max": "STAR", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [9], "_class": "Signature", "isNull": false}}'),
    (10, 4, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-9], "_class": "Signature", "isNull": false}}'),
    (10, 12, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [10], "_class": "Signature", "isNull": false}}'),
    (12, 10, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-10], "_class": "Signature", "isNull": false}}'),
    (10, 11, '{"max": "ONE", "ONE": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [11], "_class": "Signature", "isNull": false}}'),
    (11, 10, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-11], "_class": "Signature", "isNull": false}}'),
    (12, 13, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [12], "_class": "Signature", "isNull": false}}'),
    (13, 12, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-12], "_class": "Signature", "isNull": false}}'),
    (12, 14, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [13], "_class": "Signature", "isNull": false}}'),
    (14, 12, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-13], "_class": "Signature", "isNull": false}}'),
    (12, 15, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [14], "_class": "Signature", "isNull": false}}'),
    (15, 12, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-14], "_class": "Signature", "isNull": false}}');

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
    (1, 1, 4, NULL, '{"pkey": [], "kindName": "order", "accessPath": {"name": {"type": "STATIC_NAME", "value": "Order", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "_id", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "customer", "_class": "StaticName"}, "value": {"_class": "SimpleValue", "signature": {"ids": [1, 3, 2], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "number", "_class": "StaticName"}, "value": {"_class": "SimpleValue", "signature": {"ids": [4], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [0], "_class": "Signature", "isNull": true}}, {"name": {"type": "STATIC_NAME", "value": "contact", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"_class": "DynamicName", "signature": {"ids": [8, 7], "_class": "Signature", "isNull": false}}, "value": {"_class": "SimpleValue", "signature": {"ids": [6], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [5], "_class": "Signature", "isNull": false}}, {"name": {"type": "STATIC_NAME", "value": "items", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"_class": "SimpleValue", "signature": {"ids": [12, 10], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "name", "_class": "StaticName"}, "value": {"_class": "SimpleValue", "signature": {"ids": [13, 10], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "price", "_class": "StaticName"}, "value": {"_class": "SimpleValue", "signature": {"ids": [14, 10], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}, {"name": {"type": "STATIC_NAME", "value": "quantity", "_class": "StaticName"}, "value": {"_class": "SimpleValue", "signature": {"ids": [11], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [9], "_class": "Signature", "isNull": false}}], "signature": {"ids": [0], "_class": "Signature", "isNull": true}}}', '{"name": "Order"}'),
    (1, 2, 1, NULL, '{"pkey": [], "kindName": "customer", "accessPath": {"name": {"type": "STATIC_NAME", "value": "Customer", "_class": "StaticName"}, "_class": "ComplexProperty", "subpaths": [{"name": {"type": "STATIC_NAME", "value": "id", "_class": "StaticName"}, "value": {"_class": "SimpleValue", "signature": {"ids": [1], "_class": "Signature", "isNull": false}}, "_class": "SimpleProperty"}], "signature": {"ids": [0], "_class": "Signature", "isNull": true}}}', '{"name": "Customer"}');

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
    (1, '{"name": "Import Order", "type": "ModelToCategory", "_class": "Job", "status": "Canceled"}');

