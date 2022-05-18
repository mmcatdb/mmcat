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
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[1],"isNull":false,"_class":"Signature"}]}],"label":"customer","superId":{"_class":"Id","signatures":[{"ids":[1],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":100}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"id","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":101}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[1,-2],"isNull":false,"_class":"Signature"},{"ids":[19,3],"isNull":false,"_class":"Signature"}]}],"label":"ordered","superId":{"_class":"Id","signatures":[{"ids":[1,-2],"isNull":false,"_class":"Signature"},{"ids":[19,3],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":102}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[19],"isNull":false,"_class":"Signature"}]}],"label":"order","superId":{"_class":"Id","signatures":[{"ids":[19],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":103}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"number","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":104}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"array","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":105}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[17],"isNull":false,"_class":"Signature"},{"ids":[16,15],"isNull":false,"_class":"Signature"},{"ids":[19,-14],"isNull":false,"_class":"Signature"}]}],"label":"contact","superId":{"_class":"Id","signatures":[{"ids":[17],"isNull":false,"_class":"Signature"},{"ids":[16,15],"isNull":false,"_class":"Signature"},{"ids":[19,-14],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":106}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"value","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":107}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[16],"isNull":false,"_class":"Signature"}]}],"label":"type","superId":{"_class":"Id","signatures":[{"ids":[16],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":108}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"name","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":109}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"nestedDoc","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":110}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"propertyA","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":111}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"propertyB","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":112}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"propertyC","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":113}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[10,9],"isNull":false,"_class":"Signature"},{"ids":[19,-8],"isNull":false,"_class":"Signature"}]}],"label":"items","superId":{"_class":"Id","signatures":[{"ids":[10,9],"isNull":false,"_class":"Signature"},{"ids":[19,-8],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":114}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"quantity","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":115}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[10],"isNull":false,"_class":"Signature"}]}],"label":"product","superId":{"_class":"Id","signatures":[{"ids":[10],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":116}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"id","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":117}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"name","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":118}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"price","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":119}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[21],"isNull":false,"_class":"Signature"},{"ids":[19,-20],"isNull":false,"_class":"Signature"}]}],"label":"address","superId":{"_class":"Id","signatures":[{"ids":[21],"isNull":false,"_class":"Signature"},{"ids":[19,-20],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":120}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"label","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":121}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"content","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":122}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"text","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":123}}'),
    ('{"ids":[{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]}],"label":"locale","superId":{"_class":"Id","signatures":[{"ids":[],"isNull":false,"_class":"Signature"}]},"_class":"SchemaObject","key":{"_class":"Key","value":124}}');

INSERT INTO schema_object_in_category (schema_category_id, schema_object_id, position)
VALUES
    (1, 1, '{"x":-346,"y":29,"_class":"Position"}'),
    (1, 2, '{"x":-343,"y":102,"_class":"Position"}'),
    (1, 3, '{"x":-203,"y":52,"_class":"Position"}'),
    (1, 4, '{"x":15,"y":143,"_class":"Position"}'),
    (1, 5, '{"x":-39,"y":25,"_class":"Position"}'),
    (1, 6, '{"x":84,"y":25,"_class":"Position"}'),
    (1, 7, '{"x":188,"y":129,"_class":"Position"}'),
    (1, 8, '{"x":190,"y":48,"_class":"Position"}'),
    (1, 9, '{"x":351,"y":133,"_class":"Position"}'),
    (1, 10, '{"x":352,"y":47,"_class":"Position"}'),
    (1, 11, '{"x":-51,"y":247,"_class":"Position"}'),
    (1, 12, '{"x":-161,"y":329,"_class":"Position"}'),
    (1, 13, '{"x":-60,"y":334,"_class":"Position"}'),
    (1, 14, '{"x":27,"y":320,"_class":"Position"}'),
    (1, 15, '{"x":166,"y":204,"_class":"Position"}'),
    (1, 16, '{"x":397,"y":229,"_class":"Position"}'),
    (1, 17, '{"x":180,"y":331,"_class":"Position"}'),
    (1, 18, '{"x":80,"y":376,"_class":"Position"}'),
    (1, 19, '{"x":349,"y":340,"_class":"Position"}'),
    (1, 20, '{"x":259,"y":390,"_class":"Position"}'),
    (1, 21, '{"x":-270,"y":178,"_class":"Position"}'),
    (1, 22, '{"x":-221,"y":274,"_class":"Position"}'),
    (1, 23, '{"x":-346,"y":225,"_class":"Position"}'),
    (1, 24, '{"x":-308,"y":306,"_class":"Position"}'),
    (1, 25, '{"x":-419,"y":305,"_class":"Position"}');

INSERT INTO schema_morphism (domain_object_id, codomain_object_id, json_value)
VALUES
    (25, 23, '{"min":"ONE","signature":{"ids":[-24],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (24, 23, '{"min":"ONE","signature":{"ids":[-23],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (23, 21, '{"min":"ONE","signature":{"ids":[-22],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (22, 21, '{"min":"ONE","signature":{"ids":[-21],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (21, 4, '{"min":"ZERO","signature":{"ids":[-20],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (5, 4, '{"min":"ONE","signature":{"ids":[-19],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (6, 4, '{"min":"ONE","signature":{"ids":[-18],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (8, 7, '{"min":"ONE","signature":{"ids":[-17],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (10, 9, '{"min":"ONE","signature":{"ids":[-16],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (9, 7, '{"min":"ZERO","signature":{"ids":[-15],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (7, 4, '{"min":"ZERO","signature":{"ids":[-14],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (16, 15, '{"min":"ONE","signature":{"ids":[-13],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (19, 17, '{"min":"ONE","signature":{"ids":[-12],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (20, 17, '{"min":"ONE","signature":{"ids":[-11],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (18, 17, '{"min":"ONE","signature":{"ids":[-10],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (17, 15, '{"min":"ZERO","signature":{"ids":[-9],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (15, 4, '{"min":"ONE","signature":{"ids":[-8],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (14, 11, '{"min":"ONE","signature":{"ids":[-7],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (13, 11, '{"min":"ONE","signature":{"ids":[-6],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (12, 11, '{"min":"ONE","signature":{"ids":[-5],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (11, 4, '{"min":"ONE","signature":{"ids":[-4],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (4, 3, '{"min":"ONE","signature":{"ids":[-3],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (3, 1, '{"min":"ONE","signature":{"ids":[-2],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (2, 1, '{"min":"ONE","signature":{"ids":[-1],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (1, 2, '{"min":"ONE","signature":{"ids":[1],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (1, 3, '{"min":"ZERO","signature":{"ids":[2],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (3, 4, '{"min":"ONE","signature":{"ids":[3],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (4, 11, '{"min":"ONE","signature":{"ids":[4],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (11, 12, '{"min":"ONE","signature":{"ids":[5],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (11, 13, '{"min":"ONE","signature":{"ids":[6],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (11, 14, '{"min":"ONE","signature":{"ids":[7],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (4, 15, '{"min":"ZERO","signature":{"ids":[8],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (15, 17, '{"min":"ONE","signature":{"ids":[9],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (17, 18, '{"min":"ONE","signature":{"ids":[10],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (17, 20, '{"min":"ONE","signature":{"ids":[11],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (17, 19, '{"min":"ONE","signature":{"ids":[12],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (15, 16, '{"min":"ONE","signature":{"ids":[13],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (4, 7, '{"min":"ZERO","signature":{"ids":[14],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (7, 9, '{"min":"ONE","signature":{"ids":[15],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (9, 10, '{"min":"ONE","signature":{"ids":[16],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (7, 8, '{"min":"ONE","signature":{"ids":[17],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (4, 6, '{"min":"ZERO","signature":{"ids":[18],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (4, 5, '{"min":"ONE","signature":{"ids":[19],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (4, 21, '{"min":"ZERO","signature":{"ids":[20],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (21, 22, '{"min":"ONE","signature":{"ids":[21],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (21, 23, '{"min":"ONE","signature":{"ids":[22],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (23, 24, '{"min":"ONE","signature":{"ids":[23],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (23, 25, '{"min":"ONE","signature":{"ids":[24],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (10, 7, '{"min":"ZERO","signature":{"ids":[-15,-16],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (19, 15, '{"min":"ZERO","signature":{"ids":[-9,-12],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (20, 15, '{"min":"ZERO","signature":{"ids":[-9,-11],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (18, 15, '{"min":"ZERO","signature":{"ids":[-9,-10],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (5, 15, '{"min":"ZERO","signature":{"ids":[8,-19],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}'),
    (15, 18, '{"min":"ONE","signature":{"ids":[10,9],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (15, 20, '{"min":"ONE","signature":{"ids":[11,9],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (15, 19, '{"min":"ONE","signature":{"ids":[12,9],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (7, 10, '{"min":"ONE","signature":{"ids":[16,15],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (15, 5, '{"min":"ONE","signature":{"ids":[19,-8],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (4, 2, '{"min":"ONE","signature":{"ids":[1,-2,-3],"isNull":false,"_class":"Signature"},"max":"ONE","_class":"SchemaMorphism"}'),
    (2, 4, '{"min":"ZERO","signature":{"ids":[3,2,-1],"isNull":false,"_class":"Signature"},"max":"STAR","_class":"SchemaMorphism"}');

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
    (1, 29),
    (1, 30),
    (1, 31),
    (1, 32),
    (1, 33),
    (1, 34),
    (1, 35),
    (1, 36),
    (1, 37),
    (1, 38),
    (1, 39),
    (1, 40),
    (1, 41),
    (1, 42),
    (1, 43),
    (1, 44),
    (1, 45),
    (1, 46),
    (1, 47),
    (1, 48),
    (1, 49),
    (1, 50),
    (1, 51),
    (1, 52),
    (1, 53),
    (1, 54),
    (1, 55),
    (1, 56),
    (1, 57),
    (1, 58),
    (1, 59),
    (1, 60);

CREATE TABLE database_for_mapping (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO database_for_mapping (json_value)
VALUES
    ('{"type":"mongodb","label":"MongoDB","_class":"Database",
        "settings": {
            "host": "localhost",
            "port": "27017",
            "database": "mmcat_server_data",
            "authenticationDatabase": "admin",
            "username": "mmcat_user",
            "password": "mmcat_password"
        }
    }'),
    ('{"type":"postgresql","label":"PostgreSQL","_class":"Database",
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
    (1, 1, 4, NULL, '{"kindName":"order","pkey":[],"accessPath":{"signature":{"ids":[0],"isNull":true,"_class":"Signature"},"name":{"_class":"StaticName","type":"ANONYMOUS","value":""},"_class":"ComplexProperty","subpaths":[{"name":{"_class":"StaticName","type":"STATIC_NAME","value":"number"},"_class":"SimpleProperty","value":{"signature":{"ids":[19],"isNull":false,"_class":"Signature"},"_class":"SimpleValue"}}]},"_class":"Mapping"}', '{"name":"Basic mapping"}');

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
VALUES (1, '{"name":"Job Name","type":"ModelToCategory","status": "Ready"}');

