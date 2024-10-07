package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.query.Query;
import cz.matfyz.server.entity.query.QueryVersion;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class QueryRepository {

    @Autowired
    private DatabaseWrapper db;

    public record QueryWithVersion(Query query, QueryVersion version) implements Comparable<QueryWithVersion> {
        @Override public int compareTo(QueryWithVersion other) {
            return query.compareTo(other.query);
        }
    }

    private static QueryWithVersion queryWithVersionFromResultSet(ResultSet resultSet, Id queryId, Id categoryId) throws SQLException, JsonProcessingException {
        final String queryJsonValue = resultSet.getString("query_json_value");
        final Id versionId = getId(resultSet, "version_id");
        final String versionJsonValue = resultSet.getString("version_json_value");

        return new QueryWithVersion(
            Query.fromJsonValue(queryId, categoryId, queryJsonValue),
            QueryVersion.fromJsonValue(versionId, queryId, versionJsonValue)
        );
    }

    public List<QueryWithVersion> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT DISTINCT ON (query.id)
                    query.id AS query_id,
                    query.json_value AS query_json_value,
                    query_version.id AS version_id,
                    query_version.json_value AS version_json_value
                FROM query
                JOIN query_version ON query_version.query_id = query.id
                WHERE query.category_id = ?
                ORDER BY query.id, query_version.json_value::json->>'version' DESC
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final var queryId = getId(resultSet, "query_id");
                output.add(queryWithVersionFromResultSet(resultSet, queryId, categoryId));
            }
        });
    }

    public List<QueryWithVersion> findAllInCategoryWithVersion(Id categoryId, Version version) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    query.id AS query_id,
                    query.json_value AS query_json_value,
                    query_version.id AS version_id,
                    query_version.json_value AS version_json_value
                FROM query
                JOIN query_version ON query_version.query_id = query.id
                WHERE
                    query.category_id = ? AND
                    query_version.json_value->>'version' = ?
                ORDER BY query.id DESC
                """);
            setId(statement, 1, categoryId);
            statement.setString(2, version.toString());
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final var queryId = getId(resultSet, "query_id");
                output.add(queryWithVersionFromResultSet(resultSet, queryId, categoryId));
            }
        });
    }

    public QueryWithVersion find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT DISTINCT ON (query.id)
                    query.category_id AS category_id,
                    query.json_value AS query_json_value,
                    query_version.id AS version_id,
                    query_version.json_value AS version_json_value
                FROM query
                JOIN query_version ON query_version.query_id = query.id
                WHERE query.id = ?
                ORDER BY query.id, query_version.json_value::json->>'version' DESC
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var categoryId = getId(resultSet, "category_id");
                output.set(queryWithVersionFromResultSet(resultSet, id, categoryId));
            }
        });
    }

    public QueryVersion findVersion(Id versionId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    query_version.query_id AS query_id,
                    query_version.json_value AS json_value
                FROM query_version
                WHERE query_version.id = ?
                """);
            setId(statement, 1, versionId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final var queryId = getId(resultSet, "query_id");
                final var jsonValue = resultSet.getString("json_value");
                output.set(QueryVersion.fromJsonValue(versionId, queryId, jsonValue));
            }
        });
    }

    public List<QueryVersion> findAllVersionsByQuery(Id queryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    query_version.id AS id,
                    query_version.json_value AS json_value
                FROM query_version
                WHERE query_version.query_id = ?
                """);
            setId(statement, 1, queryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id id = getId(resultSet, "id");
                final String jsonValue = resultSet.getString("json_value");
                output.add(QueryVersion.fromJsonValue(id, queryId, jsonValue));
            }
        });
    }

    public void save(Query query) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO query (id, category_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    category_id = EXCLUDED.category_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, query.id());
            setId(statement, 2, query.categoryId);
            statement.setString(3, query.toJsonValue());
            executeChecked(statement);
        });
    }

    public void save(QueryVersion version) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO query_version (id, query_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    query_id = EXCLUDED.query_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, version.id());
            setId(statement, 2, version.queryId);
            statement.setString(3, version.toJsonValue());
            executeChecked(statement);
        });
    }

    public void deleteQuery(Id id) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                DELETE FROM query
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            executeChecked(statement);
        });
    }

    public void deleteQueryVersionsByQuery(Id queryId) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                DELETE FROM query_version
                WHERE query_id = ?;
                """);
            setId(statement, 1, queryId);
            executeChecked(statement);
        });
    }

}
