package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingEntity;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MappingRepository {

    @Autowired
    private DatabaseWrapper db;

    private static MappingEntity fromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        final Id id = getId(resultSet, "id");
        final Version version = Version.fromString(resultSet.getString("version"));
        final Version lastValid = Version.fromString(resultSet.getString("last_valid"));
        final Id categoryId = getId(resultSet, "category_id");
        final Id datasourceId = getId(resultSet, "datasource_id");
        final String jsonValue = resultSet.getString("json_value");

        return MappingEntity.fromJsonValue(id, version, lastValid, categoryId, datasourceId, jsonValue);
    }

    public MappingEntity find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    version,
                    last_valid,
                    category_id,
                    datasource_id,
                    json_value
                FROM mapping
                WHERE mapping.id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        }, "Mapping", id);
    }

    public List<MappingEntity> findAll() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    version,
                    last_valid,
                    category_id,
                    datasource_id,
                    json_value
                FROM mapping
                ORDER BY mapping.id;
                """);
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public List<MappingEntity> findAllInCategory(Id categoryId) {
        return findAllInCategory(categoryId, null);
    }

    public List<MappingEntity> findAllInCategory(Id categoryId, @Nullable Id datasourceId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    id,
                    version,
                    last_valid,
                    category_id,
                    datasource_id,
                    json_value
                FROM mapping
                WHERE category_id = ?
                """ + (datasourceId != null ? "AND datasource_id = ?\n" : "") + """
                ORDER BY mapping.id;
                """);
            setId(statement, 1, categoryId);
            if (datasourceId != null)
                setId(statement, 2, datasourceId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public void save(MappingEntity entity) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO mapping (id, version, last_valid, category_id, datasource_id, json_value)
                VALUES (?, ?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    version = EXCLUDED.version,
                    last_valid = EXCLUDED.last_valid,
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, entity.id());
            statement.setString(2, entity.version().toString());
            statement.setString(3, entity.lastValid().toString());
            setId(statement, 4, entity.categoryId);
            setId(statement, 5, entity.datasourceId);
            statement.setString(6, entity.toJsonValue());
            executeChecked(statement);
        });
    }

}
