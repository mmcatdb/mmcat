package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModel;
import cz.cuni.matfyz.server.entity.logicalmodel.LogicalModelInit;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

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

    public List<LogicalModel> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM logical_model WHERE schema_category_id = ? ORDER BY id;");
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                Id databaseId = getId(resultSet, "database_id");
                String jsonValue = resultSet.getString("json_value");

                output.add(LogicalModel.fromJsonValue(foundId, categoryId, databaseId, jsonValue));
            }
        });
    }

    public LogicalModel find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM logical_model WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                Id categoryId = getId(resultSet, "schema_category_id");
                Id databaseId = getId(resultSet, "database_id");
                String jsonValue = resultSet.getString("json_value");

                output.set(LogicalModel.fromJsonValue(foundId, categoryId, databaseId, jsonValue));
            }
        },
        "Logical model with id: %s not found.", id);
    }

    public Id add(LogicalModelInit init) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO logical_model (schema_category_id, database_id, json_value)
                VALUES (?, ?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            setId(statement, 1, init.categoryId());
            setId(statement, 2, init.databaseId());
            statement.setString(3, init.toJsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
