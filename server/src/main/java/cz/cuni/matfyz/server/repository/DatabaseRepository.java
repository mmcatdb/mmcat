package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.Database;
import cz.cuni.matfyz.server.entity.MappingWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;



/**
 * 
 * @author jachym.bartik
 */
@Repository
public class DatabaseRepository
{
    public Database find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM database_for_mapping WHERE id = ?;");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                String jsonValue = resultSet.getString("jsonValue");
                output.set(Database.fromJSON(foundId, jsonValue));
            }
        });
    }
}
