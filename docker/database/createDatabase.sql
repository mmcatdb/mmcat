DROP TABLE IF EXISTS query_version;
DROP TABLE IF EXISTS query;
DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS run;
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS action;
DROP TABLE IF EXISTS mapping;
DROP TABLE IF EXISTS logical_model;
DROP TABLE IF EXISTS data_source;
DROP TABLE IF EXISTS database_for_mapping;

DROP TABLE IF EXISTS schema_category_update;
DROP TABLE IF EXISTS schema_category;

-- Incrementation of the sequnce for generating ids:
-- SELECT nextval('tableName_seq_id')

-- TODO name to label

CREATE TABLE schema_category (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

CREATE TABLE schema_category_update (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

CREATE TABLE database_for_mapping (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

CREATE TABLE data_source (
    id SERIAL PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO data_source (json_value)
VALUES
    ('{
        "url": "https://nosql.ms.mff.cuni.cz/mmcat/data-sources/test2.jsonld",
        "label": "Czech business registry",
        "type": "JsonLdStore"
    }');

CREATE TABLE logical_model (
    id SERIAL PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    database_id INTEGER NOT NULL REFERENCES database_for_mapping,
    json_value JSONB NOT NULL
);

CREATE TABLE mapping (
    id SERIAL PRIMARY KEY,
    logical_model_id INTEGER NOT NULL REFERENCES logical_model,
    json_value JSONB NOT NULL
);

-- databázový systém může obsahovat více databázových instancí
    -- - v jedné db instanci musí být jména kindů atd unikátní

-- Property kindName is supposed to have the same value as the static name of the root property.
-- The reasons are that:
--      a) Sometimes we want to show only the label of the mapping, so we use the kindName for it without the necessity to access whole access path.
--      b) Some display components on the frontent use only the access path, so the information should be there.

CREATE TABLE action (
    id UUID PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

CREATE TABLE session (
    id UUID PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL,
    instance_data JSONB DEFAULT NULL
);

CREATE TABLE run (
    id UUID PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    action_id UUID REFERENCES action
);

CREATE TABLE job (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES run,
    json_value JSONB NOT NULL
);

CREATE TABLE query (
    id UUID PRIMARY KEY,
    schema_category_id INTEGER NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

CREATE TABLE query_version (
    id UUID PRIMARY KEY,
    query_id UUID NOT NULL REFERENCES query,
    json_value JSONB NOT NULL
);

-- INSERT INTO job (schema_category_id, logical_model_id, data_source_id, json_value)
-- VALUES
--     (1, '{"label": "Import Order", "state": "Ready", "payload: {
--         "type": "ModelToCategory",
--         "logicalModelId": 1
--     }}'),
--     (1, '{"label": "Export Order", "state": "Ready", "payload: {
--         "type": "CategoryToModel",
--         "logicalModelId": 1
--     }}'),
--     (1, '{"label": "Import Customer", "state": "Ready", "payload: {
--         "type": "ModelToCategory",
--         "logicalModelId": 2
--     }}'),
--     (1, '{"label": "Export Customer", "state": "Ready", "payload: {
--         "type": "CategoryToModel",
--         "logicalModelId": 2
--     }}'),
--     (1, '{"label": "Import Friend", "state": "Ready", "payload: {
--         "type": "ModelToCategory",
--         "logicalModelId": 3
--     }}'),
--     (1, '{"label": "Export Friend", "state": "Ready", "payload: {
--         "type": "CategoryToModel",
--         "logicalModelId": 3
--     }}'),
--     (2, '{"label": "Import from Postgres", "state": "Ready", "payload: {
--         "type": "ModelToCategory",
--         "logicalModelId": 4
--     }}'),
--     (2, '{"label": "Export to Mongo", "state": "Ready", "payload: {
--         "type": "CategoryToModel",
--         "logicalModelId": 5
--     }}');
