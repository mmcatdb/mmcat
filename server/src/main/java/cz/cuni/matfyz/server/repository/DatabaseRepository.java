package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.database.Database;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * 
 * @author jachym.bartik
 */
@Repository
public class DatabaseRepository {

    public Database find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                output.set(Database.fromJSONValue(id, jsonValue));
            }
        });
    }

    public List<Database> findAll() {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping ORDER BY id;");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String jsonValue = resultSet.getString("json_value");
                output.add(Database.fromJSONValue(id, jsonValue));
            }
        });
    }

    public Database save(Database database) {
        return database.id == null ? create(database) : update(database);
    }

    private Database create(Database database) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("INSERT INTO database_for_mapping (json_value) VALUES (?::jsonb);", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, database.toJSONValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt("id");
                output.set(new Database(id, database));
            }
        });
    }

    private Database update(Database database) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("UPDATE database_for_mapping SET json_value = ?::jsonb WHERE id = ?;");
            statement.setString(1, database.toJSONValue());
            statement.setInt(2, database.id);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            output.set(new Database(database.id, database));
        });
    }

    public boolean delete(int id) {
        return DatabaseWrapper.getBoolean((connection, output) -> {
            var statement = connection.prepareStatement("DELETE FROM database_for_mapping WHERE id = ?;");
            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();
            output.set(affectedRows == 1);
        });
    }

}
