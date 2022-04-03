package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Database;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

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
                int foundId = resultSet.getInt("id");
                String jsonValue = resultSet.getString("json_value");
                output.set(Database.fromJSON(foundId, jsonValue));
            }
        });
    }

    public List<Database> findAll() {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping;");
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                String jsonValue = resultSet.getString("json_value");
                output.add(Database.fromJSON(foundId, jsonValue));
            }
        });
    }

}
