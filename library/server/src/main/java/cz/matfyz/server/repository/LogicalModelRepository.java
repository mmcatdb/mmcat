package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.LogicalModel;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LogicalModelRepository {

    @Autowired
    private DatabaseWrapper db;

    public record LogicalModelWithDatasource(
        LogicalModel logicalModel,
        DatasourceWrapper datasource
    ) {}

    private static LogicalModelWithDatasource fromResultSet(ResultSet resultSet) throws SQLException, JsonProcessingException {
        final Id logicalModelId = getId(resultSet, "logical_model_id");
        final Id categoryId = getId(resultSet, "category_id");
        final String logicalModelJsonValue = resultSet.getString("logical_model_json_value");
        final Id datasourceId = getId(resultSet, "datasource_id");
        final String datasourceJsonValue = resultSet.getString("datasource_json_value");

        return new LogicalModelWithDatasource(
            LogicalModel.fromJsonValue(logicalModelId, categoryId, datasourceId, logicalModelJsonValue),
            DatasourceWrapper.fromJsonValue(datasourceId, datasourceJsonValue)
        );
    }

    public List<LogicalModelWithDatasource> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    logical_model.id as logical_model_id,
                    logical_model.category_id as category_id,
                    logical_model.json_value as logical_model_json_value,
                    datasource.id as datasource_id,
                    datasource.json_value as datasource_json_value
                FROM logical_model
                JOIN datasource ON datasource.id = logical_model.datasource_id
                WHERE logical_model.category_id = ?
                ORDER BY logical_model.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next())
                output.add(fromResultSet(resultSet));
        });
    }

    public LogicalModelWithDatasource find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    logical_model.id as logical_model_id,
                    logical_model.category_id as category_id,
                    logical_model.json_value as logical_model_json_value,
                    datasource.id as datasource_id,
                    datasource.json_value as datasource_json_value
                FROM logical_model
                JOIN datasource ON datasource.id = logical_model.datasource_id
                WHERE logical_model.id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        },
        "Logical model", id);
    }

    public LogicalModelWithDatasource find(Id categoryId, Id datasourceId) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    logical_model.id as logical_model_id,
                    logical_model.category_id as category_id,
                    logical_model.json_value as logical_model_json_value,
                    datasource.id as datasource_id,
                    datasource.json_value as datasource_json_value
                FROM logical_model
                JOIN datasource ON datasource.id = logical_model.datasource_id
                WHERE logical_model.category_id = ? AND
                    logical_model.datasource_id = ?;
                """);
            setId(statement, 1, categoryId);
            setId(statement, 2, datasourceId);
            final var resultSet = statement.executeQuery();

            if (resultSet.next())
                output.set(fromResultSet(resultSet));
        });
    }

    public void save(LogicalModel model) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO logical_model (id, category_id, datasource_id, json_value)
                VALUES (?, ?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, model.id());
            setId(statement, 2, model.categoryId);
            setId(statement, 3, model.datasourceId);
            statement.setString(4, model.toJsonValue());
            executeChecked(statement);
        });
    }

}
