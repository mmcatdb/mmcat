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
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [1], "_class": "Signature", "isNull": false}]}], "key": {"value": 1, "_class": "Key"}, "label": "Customer", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [1], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql", "mongodb"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 2, "_class": "Key"}, "label": "Id", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql", "mongodb"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 3, "_class": "Key"}, "label": "Name", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql", "mongodb"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 4, "_class": "Key"}, "label": "Surname", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql", "mongodb"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [4], "_class": "Signature", "isNull": false}, {"ids": [5], "_class": "Signature", "isNull": false}]}], "key": {"value": 5, "_class": "Key"}, "label": "Friend", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [5], "_class": "Signature", "isNull": false}]}, "databases": ["mongodb"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 6, "_class": "Key"}, "label": "Address", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 7, "_class": "Key"}, "label": "Street", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 8, "_class": "Key"}, "label": "City", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql"]}'),
    ('{"ids": [{"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}], "key": {"value": 9, "_class": "Key"}, "label": "Post code", "_class": "SchemaObject", "superId": {"_class": "Id", "signatures": [{"ids": [], "_class": "Signature", "isNull": false}]}, "databases": ["postgresql"]}');

INSERT INTO schema_object_in_category (schema_category_id, schema_object_id, position)
VALUES
    (1, 1, '{"x": 86, "y": -27, "_class": "Position"}'),
    (1, 2, '{"x": 271, "y": -97, "_class": "Position"}'),
    (1, 3, '{"x": 272, "y": -8, "_class": "Position"}'),
    (1, 4, '{"x": 279, "y": 70, "_class": "Position"}'),
    (1, 5, '{"x": 89, "y": -125, "_class": "Position"}'),
    (1, 6, '{"x": -52, "y": -28, "_class": "Position"}'),
    (1, 7, '{"x": -179, "y": -129, "_class": "Position"}'),
    (1, 8, '{"x": -192, "y": -29, "_class": "Position"}'),
    (1, 9, '{"x": -180, "y": 73, "_class": "Position"}');

INSERT INTO schema_morphism (domain_object_id, codomain_object_id, json_value)
VALUES
    (1, 2, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [1], "_class": "Signature", "isNull": false}}'),
    (2, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-1], "_class": "Signature", "isNull": false}}'),
    (1, 3, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [2], "_class": "Signature", "isNull": false}}'),
    (3, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-2], "_class": "Signature", "isNull": false}}'),
    (1, 4, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [3], "_class": "Signature", "isNull": false}}'),
    (4, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-3], "_class": "Signature", "isNull": false}}'),
    (5, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [4], "_class": "Signature", "isNull": false}}'),
    (1, 5, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-4], "_class": "Signature", "isNull": false}}'),
    (5, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [5], "_class": "Signature", "isNull": false}}'),
    (1, 5, '{"max": "STAR", "min": "ZERO", "_class": "SchemaMorphism", "signature": {"ids": [-5], "_class": "Signature", "isNull": false}}'),
    (6, 7, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [6], "_class": "Signature", "isNull": false}}'),
    (7, 6, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-6], "_class": "Signature", "isNull": false}}'),
    (6, 8, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [7], "_class": "Signature", "isNull": false}}'),
    (8, 6, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-7], "_class": "Signature", "isNull": false}}'),
    (6, 9, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [8], "_class": "Signature", "isNull": false}}'),
    (9, 6, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-8], "_class": "Signature", "isNull": false}}'),
    (1, 6, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [9], "_class": "Signature", "isNull": false}}'),
    (6, 1, '{"max": "ONE", "min": "ONE", "_class": "SchemaMorphism", "signature": {"ids": [-9], "_class": "Signature", "isNull": false}}');

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
    (1, 18);


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

CREATE TABLE job (
    id SERIAL PRIMARY KEY,
    mapping_id INTEGER NOT NULL REFERENCES mapping,
    json_value JSONB NOT NULL
    -- přidat typ jobu, vstup, výstup, vše serializované v jsonu
        -- podobně jako ukládání logování
        -- součástí log4j je nastavení kam se to dá ukládat, resp. do libovolné kombinace uložišť
            -- např. prometheus, zabbix, kibana - monitorování stavu aplikace

);
