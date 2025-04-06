package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.controller.SchemaCategoryController.SchemaCategoryInfo;
import cz.matfyz.server.controller.SchemaCategoryController.SchemaCategoryStats;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.SchemaCategoryWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SchemaCategoryRepository {

    @Autowired
    private DatabaseWrapper db;

    private static SchemaCategoryInfo infoFromResultSet(ResultSet resultSet) throws SQLException {
        final Id id = getId(resultSet, "id");
        final Version version = Version.fromString(resultSet.getString("version"));
        final Version lastValid = Version.fromString(resultSet.getString("last_valid"));
        final String label = resultSet.getString("label");
        final Version systemVersion = Version.fromString(resultSet.getString("system_version"));

        return new SchemaCategoryInfo(id, version, lastValid, label, systemVersion);
    }

    public List<SchemaCategoryInfo> findAllInfos() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.createStatement();
            final var resultSet = statement.executeQuery("""
                SELECT
                    id,
                    version,
                    last_valid,
                    label,
                    system_version
                FROM schema_category
                ORDER BY updated_at DESC;
                """);

            while (resultSet.next())
                output.add(infoFromResultSet(resultSet));
        });
    }

    public SchemaCategoryInfo findInfo(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    version,
                    last_valid,
                    label,
                    system_version
                FROM schema_category
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(infoFromResultSet(resultSet));
        });
    }

    public SchemaCategoryWrapper find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    version,
                    last_valid,
                    label,
                    system_version,
                    json_value
                FROM schema_category
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var version = Version.fromString(resultSet.getString("version"));
                final var lastValid = Version.fromString(resultSet.getString("last_valid"));
                final var label = resultSet.getString("label");
                final var systemVersion = Version.fromString(resultSet.getString("system_version"));
                final var jsonValue = resultSet.getString("json_value");
                output.set(SchemaCategoryWrapper.fromJsonValue(id, version, lastValid, label, systemVersion, jsonValue));
            }
        });
    }

    public SchemaCategoryStats findStats(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                WITH objects_count AS (
                    SELECT jsonb_array_length(schema_category.json_value->'schema'->'objects') AS objects
                    FROM schema_category
                    WHERE schema_category.id = ?
                ),
                mappings_count AS (
                    SELECT COUNT(DISTINCT mapping.id) AS mappings
                    FROM mapping
                    WHERE mapping.category_id = ?
                ),
                jobs_count AS (
                    SELECT COUNT(DISTINCT job.id) AS jobs
                    FROM job
                    JOIN run ON job.run_id = run.id
                    WHERE run.category_id = ?
                )
                SELECT
                    objects_count.objects,
                    mappings_count.mappings,
                    jobs_count.jobs
                FROM objects_count, mappings_count, jobs_count;
                """);
            setId(statement, 1, id);
            setId(statement, 2, id);
            setId(statement, 3, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var objects = resultSet.getInt("objects");
                final var mappings = resultSet.getInt("mappings");
                final var jobs = resultSet.getInt("jobs");
                output.set(new SchemaCategoryStats(objects, mappings, jobs));
            }
        });
    }

    public void save(SchemaCategoryWrapper wrapper) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO schema_category (id, version, last_valid, label, system_version, updated_AT, json_value)
                VALUES (?, ?, ?, ?, ?, NOW(), ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    version = EXCLUDED.version,
                    last_valid = EXCLUDED.last_valid,
                    label = EXCLUDED.label,
                    system_version = EXCLUDED.system_version,
                    updated_at = EXCLUDED.updated_at,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, wrapper.id());
            statement.setString(2, wrapper.version().toString());
            statement.setString(3, wrapper.lastValid().toString());
            statement.setString(4, wrapper.label);
            statement.setString(5, wrapper.systemVersion().toString());
            statement.setString(6, wrapper.toJsonValue());
            executeChecked(statement);
        });
    }

    public void delete(Id id) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                DELETE FROM schema_category
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            executeChecked(statement);
        });
    }

}
