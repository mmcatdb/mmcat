-- psql postgresql://mmcat_user:mmcat_password@localhost/mmcat_server_dml?sslmode=require -f src/main/resources/setupPostgresqlDML.sql

DROP TABLE IF EXISTS app_order;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    u_id TEXT PRIMARY KEY,
    name TEXT
);

CREATE TABLE app_order (
    o_id TEXT PRIMARY KEY,
    u_id TEXT REFERENCES app_user(u_id)
);

INSERT INTO app_user (u_id, name)
VALUES
    ('1', 'Alice'),
    ('2', 'Bob');

INSERT INTO app_order (o_id, u_id)
VALUES
    ('1', '1'),
    ('2', '1'),
    ('3', '1'),
    ('4', '2');