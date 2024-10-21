package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.LogicalModel;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.repository.LogicalModelRepository.LogicalModelWithDatasource;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class DatasourceRepository {

    @Autowired
    private DatabaseWrapper db;

    private static DatasourceWrapper fromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        final Id id = getId(resultSet, "id");
        final String jsonValue = resultSet.getString("json_value");

        return DatasourceWrapper.fromJsonValue(id, jsonValue);
    }

    public DatasourceWrapper find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("SELECT * FROM datasource WHERE id = ?;");
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        },
        "Datasource", id);
    }

    public List<DatasourceWrapper> findAll() {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("SELECT * FROM datasource ORDER BY id;");
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public List<DatasourceWrapper> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    DISTINCT datasource.id as id,
                    datasource.json_value as json_value
                FROM datasource
                JOIN logical_model on logical_model.datasource_id = datasource.id
                WHERE logical_model.category_id = ?
                ORDER BY datasource.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public void save(DatasourceWrapper datasource) {
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

