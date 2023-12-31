package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseEntity;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

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

    public DatabaseEntity find(Id id) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                output.set(DatabaseEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DatabaseEntity> findAll() {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping ORDER BY id;");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id id = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                output.add(DatabaseEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public List<DatabaseEntity> findAllInCategory(Id categoryId) {
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
                output.add(DatabaseEntity.fromJsonValue(id, jsonValue));
            }
        });
    }

    public DatabaseEntity save(DatabaseEntity database) {
        return database.id == null ? create(database) : update(database);
    }

    private DatabaseEntity create(DatabaseEntity database) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO database_for_mapping (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, database.toJsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Id id = getId(generatedKeys, "id");
                output.set(new DatabaseEntity(id, database));
            }
        });
    }

    private DatabaseEntity update(DatabaseEntity database) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("UPDATE database_for_mapping SET json_value = ?::jsonb WHERE id = ?;");
            statement.setString(1, database.toJsonValue());
            setId(statement, 2, database.id);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            output.set(new DatabaseEntity(database.id, database));
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
