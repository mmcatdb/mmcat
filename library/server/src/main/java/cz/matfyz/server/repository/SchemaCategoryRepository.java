package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SchemaCategoryRepository {

    @Autowired
    private DatabaseWrapper db;

    public List<SchemaCategoryInfo> findAllInfos() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.createStatement();
            final var resultSet = statement.executeQuery("""
                SELECT
                    id,
                    json_value::json->>'label' as label,
                    json_value::json->>'version' as version,
                    json_value::json->>'systemVersion' as systemVersion
                FROM schema_category
                ORDER BY id;
                """);

            while (resultSet.next()) {
                final var id = getId(resultSet, "id");
                final var label = resultSet.getString("label");
                final var version = Version.fromString(resultSet.getString("version"));
                final var systemVersion = Version.fromString(resultSet.getString("systemVersion"));
                output.add(new SchemaCategoryInfo(id, label, version, systemVersion));
            }
        });
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    json_value::json->>'label' as label,
                    json_value::json->>'version' as version,
                    json_value::json->>'systemVersion' as systemVersion
                FROM schema_category
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var label = resultSet.getString("label");
                final var version = Version.fromString(resultSet.getString("version"));
                final var systemVersion = Version.fromString(resultSet.getString("systemVersion"));
                output.set(new SchemaCategoryInfo(id, label, version, systemVersion));
            }
        });
    }

    public SchemaCategoryWrapper find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("SELECT * FROM schema_category WHERE id = ?;");
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var jsonValue = resultSet.getString("json_value");
                output.set(SchemaCategoryWrapper.fromJsonValue(id, jsonValue));
            }
        });
    }

    public void add(SchemaCategoryWrapper wrapper) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO schema_category (json_value)
                VALUES (?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, wrapper.toJsonValue());
            executeChecked(statement);

            final var generatedKeys = statement.getGeneratedKeys();
            generatedKeys.next();
            wrapper.assignId(getId(generatedKeys, "id"));
        });
    }

    public void save(SchemaCategoryWrapper wrapper) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                UPDATE schema_category
                SET json_value = ?::jsonb
                WHERE id = ?;
                """);
            statement.setString(1, wrapper.toJsonValue());
            setId(statement, 2, wrapper.id());
            executeChecked(statement);
        });
    }

    public void update(SchemaCategoryWrapper wrapper, SchemaUpdate update) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                UPDATE schema_category
                SET json_value = ?::jsonb
                WHERE id = ?;
                INSERT INTO schema_category_update (schema_category_id, json_value)
                VALUES (?, ?::jsonb);
                """);
            statement.setString(1, wrapper.toJsonValue());
            setId(statement, 2, wrapper.id());
            setId(statement, 3, wrapper.id());
            statement.setString(4, update.toJsonValue());
            executeChecked(statement);
        });
    }

    public void updateMetadata(SchemaCategoryWrapper wrapper) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                UPDATE schema_category
                SET json_value = ?::jsonb
                WHERE id = ?;
                """);
            statement.setString(1, wrapper.toJsonValue());
            setId(statement, 2, wrapper.id());
            executeChecked(statement);
        });
    }

    public List<SchemaUpdate> findAllUpdates(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    json_value
                FROM schema_category_update
                WHERE schema_category_id = ?
                ORDER BY id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final var id = getId(resultSet, "id");
                final var jsonValue = resultSet.getString("json_value");
                output.add(SchemaUpdate.fromJsonValue(id, categoryId, jsonValue));
            }
        });
    }

}
