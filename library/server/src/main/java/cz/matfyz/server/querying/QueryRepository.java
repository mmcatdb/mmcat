package cz.matfyz.server.querying;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.utils.DatabaseWrapper;
import cz.matfyz.server.utils.entity.Id;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static cz.matfyz.server.utils.Utils.*;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class QueryRepository {

    @Autowired
    private DatabaseWrapper db;

    private static Query fromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        final Id queryId = getId(resultSet, "id");
        final Version version = Version.fromString(resultSet.getString("version"));
        final Version lastValid = Version.fromString(resultSet.getString("last_valid"));
        final Id categoryId = getId(resultSet, "category_id");
        final String jsonValue = resultSet.getString("json_value");

        return Query.fromJsonValue(queryId, version, lastValid, categoryId, jsonValue);
    }

    /**
     * @param version If provided, the query will be filtered by the version.
     */
    public List<Query> findAllInCategory(Id categoryId, @Nullable Version version) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    version,
                    last_valid,
                    category_id,
                    json_value
                FROM query
                WHERE query.category_id = ?
                """ + (version != null ? "AND query.version = ?\n" : "") + """
                ORDER BY query.id
                """);
            setId(statement, 1, categoryId);
            if (version != null)
                statement.setString(2, version.toString());
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public Query find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    version,
                    last_valid,
                    category_id,
                    json_value
                FROM query
                WHERE query.id = ?
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        });
    }

    public void save(Query query) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO query (id, version, last_valid, category_id, json_value)
                VALUES (?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    version = EXCLUDED.version,
                    last_valid = EXCLUDED.last_valid,
                    category_id = EXCLUDED.category_id,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, query.id());
            statement.setString(2, query.version().toString());
            statement.setString(3, query.lastValid().toString());
            setId(statement, 4, query.categoryId);
            statement.setString(5, query.toJsonValue());
            executeChecked(statement);
        });
    }

    public void delete(Id id) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                DELETE FROM query
                WHERE id = ?;
                """);
            setId(statement, 1, id);
            executeChecked(statement);
        });
    }

}
