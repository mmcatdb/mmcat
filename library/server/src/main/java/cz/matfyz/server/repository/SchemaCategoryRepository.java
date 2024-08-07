package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.evolution.SchemaUpdate;
import cz.matfyz.server.entity.schema.SchemaCategoryInfo;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;
import cz.matfyz.server.repository.utils.Utils;

import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SchemaCategoryRepository {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectReader schemaObjectWrapperJsonReader = mapper.readerFor(SchemaObjectWrapper.class);

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

    public Id add(SchemaCategoryWrapper wrapper) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO schema_category (json_value)
                VALUES (?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, wrapper.toJsonValue());

            final int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            final var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

    public boolean save(SchemaCategoryWrapper wrapper) {
        return db.get((connection, output) -> {
        final var statement = connection.prepareStatement("""
            UPDATE schema_category
            SET json_value = ?::jsonb
            WHERE id = ?;
            """);
        statement.setString(1, wrapper.toJsonValue());
        setId(statement, 2, wrapper.id);

        final int affectedRows = statement.executeUpdate();
        output.set(affectedRows != 0);
        });
    }

    public boolean update(SchemaCategoryWrapper wrapper, SchemaUpdate update) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                UPDATE schema_category
                SET json_value = ?::jsonb
                WHERE id = ?;
                INSERT INTO schema_category_update (schema_category_id, json_value)
                VALUES (?, ?::jsonb);
                """);
            statement.setString(1, wrapper.toJsonValue());
            setId(statement, 2, wrapper.id);
            setId(statement, 3, wrapper.id);
            statement.setString(4, update.toJsonValue());

            final int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
        });
    }

    public boolean updateMetadata(SchemaCategoryWrapper wrapper) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                UPDATE schema_category
                SET json_value = ?::jsonb
                WHERE id = ?;
                """);
            statement.setString(1, wrapper.toJsonValue());
            setId(statement, 2, wrapper.id);

            final int affectedRows = statement.executeUpdate();
            output.set(affectedRows != 0);
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

    public SchemaObjectWrapper findObject(Id categoryId, Key key) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT object
                FROM
                    (SELECT * FROM schema_category WHERE id = ?) as selected_category,
                    jsonb_array_elements(selected_category.json_value->'objects') object
                WHERE object->'key' @> ?;
                """);
            setId(statement, 1, categoryId);
            statement.setString(2, Utils.toJson(key));
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var jsonObject = resultSet.getString("object");
                output.set(schemaObjectWrapperJsonReader.readValue(jsonObject));
            }
        });
    }

}
