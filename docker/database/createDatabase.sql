DROP TABLE IF EXISTS query_version;
DROP TABLE IF EXISTS query;
DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS run;
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS action;
DROP TABLE IF EXISTS mapping;
DROP TABLE IF EXISTS logical_model;
DROP TABLE IF EXISTS datasource;

DROP TABLE IF EXISTS schema_category_update;
DROP TABLE IF EXISTS schema_category;

-- Incrementation of the sequnce for generating ids:
-- SELECT nextval('tableName_seq_id')

-- TODO name to label

CREATE TABLE schema_category (
    id UUID PRIMARY KEY,
    json_value JSONB NOT NULL
);

CREATE TABLE schema_category_update (
    id UUID PRIMARY KEY,
    schema_category_id UUID NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

CREATE TABLE datasource (
    id UUID PRIMARY KEY,
    json_value JSONB NOT NULL
);

INSERT INTO datasource (id, json_value)
VALUES
    ('119dd3fc-aabd-4195-9d12-94abf4fceeb0', '{
        "label": "Czech business registry",
        "type": "jsonld",
        "settings": {
            "url": "https://data.mmcatdb.com/test2.jsonld",
            "isWritable": false,
            "isQueryable": false
        }
    }');

CREATE TABLE logical_model (
    id UUID PRIMARY KEY,
    schema_category_id UUID NOT NULL REFERENCES schema_category,
    datasource_id UUID NOT NULL REFERENCES datasource,
    json_value JSONB NOT NULL
);

CREATE TABLE mapping (
    id UUID PRIMARY KEY,
    logical_model_id UUID NOT NULL REFERENCES logical_model,
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
    schema_category_id UUID NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

CREATE TABLE session (
    id UUID PRIMARY KEY,
    schema_category_id UUID NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL,
    instance_data JSONB DEFAULT NULL
);

CREATE TABLE run (
    id UUID PRIMARY KEY,
    schema_category_id UUID NOT NULL REFERENCES schema_category,
    action_id UUID REFERENCES action,
    session_id UUID REFERENCES session
);

CREATE TABLE job (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES run,
    json_value JSONB NOT NULL
);

CREATE TABLE query (
    id UUID PRIMARY KEY,
    schema_category_id UUID NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

CREATE TABLE query_version (
    id UUID PRIMARY KEY,
    query_id UUID NOT NULL REFERENCES query,
    json_value JSONB NOT NULL
);
