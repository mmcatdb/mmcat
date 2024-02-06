package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import com.fasterxml.jackson.core.JsonProcessingException;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class LogicalModelRepository {

    @Autowired
    private DatabaseWrapper db;

    public record LogicalModelWithDatabase(
        LogicalModel logicalModel,
        DatabaseEntity database
    ) {}

    private static LogicalModelWithDatabase modelFromResultSet(ResultSet resultSet, Id modelId, Id categoryId) throws SQLException, JsonProcessingException {
        final String modelJsonValue = resultSet.getString("logical_model.json_value");
        final Id databaseId = getId(resultSet, "database.id");
        final String databaseJsonValue = resultSet.getString("database.json_value");

        return new LogicalModelWithDatabase(
            LogicalModel.fromJsonValue(modelId, categoryId, databaseId, modelJsonValue),
            DatabaseEntity.fromJsonValue(databaseId, databaseJsonValue)
        );
    }

    public List<LogicalModelWithDatabase> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    logical_model.id as "logical_model.id",
                    logical_model.json_value as "logical_model.json_value",
                    database_for_mapping.id as "database.id",
                    database_for_mapping.json_value as "database.json_value"
                FROM logical_model
                JOIN database_for_mapping ON database_for_mapping.id = logical_model.database_id
                WHERE logical_model.schema_category_id = ?
                ORDER BY logical_model.id;
                """);
            setId(statement, 1, categoryId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id modelId = getId(resultSet, "logical_model.id");
                output.add(modelFromResultSet(resultSet, modelId, categoryId));
            }
        });
    }

    public LogicalModelWithDatabase find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    logical_model.schema_category_id as "logical_model.schema_category_id",
                    logical_model.json_value as "logical_model.json_value",
                    database_for_mapping.id as "database.id",
                    database_for_mapping.json_value as "database.json_value"
                FROM logical_model
                JOIN database_for_mapping ON database_for_mapping.id = logical_model.database_id
                WHERE logical_model.id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final Id categoryId = getId(resultSet, "logical_model.schema_category_id");
                output.set(modelFromResultSet(resultSet, id, categoryId));
            }
        },
        "Logical model with id: %s not found.", id);
    }

    public Id add(LogicalModelInit init) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO logical_model (schema_category_id, database_id, json_value)
                VALUES (?, ?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            setId(statement, 1, init.categoryId());
            setId(statement, 2, init.databaseId());
            statement.setString(3, init.toJsonValue());

            final int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            final var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
