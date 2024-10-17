DROP TABLE IF EXISTS workflow;

DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS run;
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS action;

DROP TABLE IF EXISTS query_version;
DROP TABLE IF EXISTS mapping_update;
DROP TABLE IF EXISTS schema_update;
DROP TABLE IF EXISTS evolution;

DROP TABLE IF EXISTS query;
DROP TABLE IF EXISTS mapping;
DROP TABLE IF EXISTS logical_model;
DROP TABLE IF EXISTS datasource;
DROP TABLE IF EXISTS schema_category;

-- Schema

CREATE TABLE schema_category (
    id UUID PRIMARY KEY,
    version VARCHAR(255) NOT NULL,
    last_valid VARCHAR(255) NOT NULL,
    label VARCHAR(255) NOT NULL,
    system_version VARCHAR(255) NOT NULL,
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
    category_id UUID NOT NULL REFERENCES schema_category,
    datasource_id UUID NOT NULL REFERENCES datasource,
    json_value JSONB NOT NULL
);

CREATE TABLE mapping (
    id UUID PRIMARY KEY,
    version VARCHAR(255) NOT NULL,
    last_valid VARCHAR(255) NOT NULL,
    logical_model_id UUID NOT NULL REFERENCES logical_model,
    json_value JSONB NOT NULL
);

-- Querying

CREATE TABLE query (
    id UUID PRIMARY KEY,
    version VARCHAR(255) NOT NULL,
    last_valid VARCHAR(255) NOT NULL,
    category_id UUID NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

-- Evolution

CREATE TABLE evolution (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES schema_category,
    version VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL
);

CREATE TABLE category_evolution (
    id UUID PRIMARY KEY REFERENCES evolution ON DELETE CASCADE,
    json_value JSONB NOT NULL
);

CREATE TABLE mapping_evolution (
    id UUID PRIMARY KEY REFERENCES evolution ON DELETE CASCADE,
    mapping_id UUID NOT NULL REFERENCES mapping,
    json_value JSONB NOT NULL
);

CREATE TABLE query_evolution (
    id UUID PRIMARY KEY REFERENCES evolution ON DELETE CASCADE,
    query_id UUID NOT NULL REFERENCES query,
    json_value JSONB NOT NULL
);

-- Actions

CREATE TABLE action (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL
);

CREATE TABLE session (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES schema_category,
    json_value JSONB NOT NULL,
    instance_data JSONB DEFAULT NULL
);

CREATE TABLE run (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES schema_category,
    action_id UUID REFERENCES action,
    session_id UUID REFERENCES session
);

CREATE TABLE job (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES run,
    json_value JSONB NOT NULL
);

-- Workflow

CREATE TABLE workflow (
    id UUID PRIMARY KEY,
    category_id UUID NOT NULL REFERENCES schema_category,
    label VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    json_value JSONB NOT NULL
);
