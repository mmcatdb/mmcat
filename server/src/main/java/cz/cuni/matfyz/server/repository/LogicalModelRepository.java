package cz.cuni.matfyz.server.repository;

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

    public List<LogicalModel> findAllInCategory(int categoryId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM logical_model WHERE schema_category_id = ? ORDER BY id;");
            statement.setInt(1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                int databaseId = resultSet.getInt("database_id");
                String jsonValue = resultSet.getString("json_value");

                output.add(new LogicalModel(foundId, categoryId, databaseId, jsonValue));
            }
        });
    }

    public LogicalModel find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM logical_model WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                int categoryId = resultSet.getInt("schema_category_id");
                int databaseId = resultSet.getInt("database_id");
                String jsonValue = resultSet.getString("json_value");

                output.set(new LogicalModel(foundId, categoryId, databaseId, jsonValue));
            }
        });
    }

    public Integer add(LogicalModelInit logicalModel) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO logical_model (schema_category_id, database_id, json_value)
                VALUES (?, ?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, logicalModel.categoryId());
            statement.setInt(2, logicalModel.databaseId());
            statement.setString(3, logicalModel.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(generatedKeys.getInt("id"));
        });
    }

}
