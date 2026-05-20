package cz.matfyz.server.datasource;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.utils.DatabaseWrapper;
import cz.matfyz.server.utils.entity.Id;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static cz.matfyz.server.utils.Utils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DatasourceRepository {

    @Autowired
    private DatabaseWrapper db;

    private static DatasourceEntity fromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        final Id id = getId(resultSet, "id");
        final String jsonValue = resultSet.getString("json_value");

        return DatasourceEntity.fromJsonValue(id, jsonValue);
    }

    public DatasourceEntity find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("SELECT * FROM datasource WHERE id = ?;");
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        },
        "Datasource", id);
    }

    public DatasourceEntity findByMappingId(Id mappingId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT datasource.*
                FROM datasource
                JOIN mapping ON mapping.datasource_id = datasource.id
                WHERE mapping.id = ?;
                """);
            setId(statement, 1, mappingId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        });
    }

    public List<DatasourceEntity> findAll() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("SELECT * FROM datasource ORDER BY id;");
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public List<DatasourceEntity> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    DISTINCT datasource.id as id,
                    datasource.json_value as json_value
                FROM datasource
                JOIN mapping on mapping.datasource_id = datasource.id
                WHERE mapping.category_id = ?
                ORDER BY datasource.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public void save(DatasourceEntity datasource) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO datasource (id, json_value)
                VALUES (?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, datasource.id());
            statement.setString(2, datasource.toJsonValue());
            executeChecked(statement);
        });
    }

    public void delete(Id id) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("DELETE FROM datasource WHERE id = ?;");
            setId(statement, 1, id);
            executeChecked(statement);
        });
    }

}

