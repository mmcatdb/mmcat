package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class DatabaseRepository {

    @Autowired
    private DatabaseWrapper db;

    public Database find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                output.set(Database.fromJSONValue(id, jsonValue));
            }
        });
    }

    public List<Database> findAll() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping ORDER BY id;");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(Database.fromJSONValue(id, jsonValue));
            }
        });
    }

    public List<Database> findAllInCategory(Id categoryId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                    SELECT
                        DISTINCT database_for_mapping.id as id,
                        database_for_mapping.json_value as json_value
                    FROM database_for_mapping
                    JOIN logical_model on logical_model.id = database_for_mapping.id
                    WHERE logical_model.schema_category_id = ?
                    ORDER BY database_for_mapping.id;
                """);
            setId(statement, 1, categoryId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(Database.fromJSONValue(id, jsonValue));
            }
        });
    }

    public Database save(Database database) {
        return database.id == null ? create(database) : update(database);
    }

    private Database create(Database database) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO database_for_mapping (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, database.toJSONValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Id id = getId(generatedKeys, "id");
                output.set(new Database(id, database));
            }
        });
    }

    private Database update(Database database) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("UPDATE database_for_mapping SET json_value = ?::jsonb WHERE id = ?;");
            statement.setString(1, database.toJSONValue());
            setId(statement, 2, database.id);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            output.set(new Database(database.id, database));
        });
    }

    public boolean delete(Id id) {
        return db.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("DELETE FROM database_for_mapping WHERE id = ?;");
            setId(statement, 1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows == 1);
        });
    }

}
