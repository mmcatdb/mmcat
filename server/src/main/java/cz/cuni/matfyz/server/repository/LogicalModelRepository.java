package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class LogicalModelRepository {

    public List<LogicalModel> findAllInCategory(Id categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM logical_model WHERE schema_category_id = ? ORDER BY id;");
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                Id databaseId = getId(resultSet, "database_id");
                String jsonValue = resultSet.getString("json_value");

                output.add(new LogicalModel(foundId, categoryId, databaseId, jsonValue));
            }
        });
    }

    public LogicalModel find(Id id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM logical_model WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                Id categoryId = getId(resultSet, "schema_category_id");
                Id databaseId = getId(resultSet, "database_id");
                String jsonValue = resultSet.getString("json_value");

                output.set(new LogicalModel(foundId, categoryId, databaseId, jsonValue));
            }
        });
    }

    public Id add(LogicalModelInit logicalModel) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO logical_model (schema_category_id, database_id, json_value)
                VALUES (?, ?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            setId(statement, 1, logicalModel.categoryId());
            setId(statement, 2, logicalModel.databaseId());
            statement.setString(3, logicalModel.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
