package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.MappingWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.repository.utils.Utils;

import java.util.*;
import org.springframework.stereotype.Repository;



/**
 * 
 * @author jachym.bartik
 */
@Repository
public class MappingRepository {

    public List<MappingWrapper> findAllInCategory(int categoryId) {
        var output = new ArrayList<MappingWrapper>();
        // TODO
        /*
        try
        {
            var connection = DatabaseWrapper.getConnection();
            var statement = connection.prepareStatement("""
                SELECT *
                FROM schema_object
                JOIN schema_object_in_category ON (schema_object_id = schema_object.id)
                WHERE schema_category_id = ?;
            """);
            statement.setInt(1, categoryId);
            var resultSet = statement.executeQuery();

            //var builder = new Mapping.Builder();
            while (resultSet.next())
            {
                //var jsonObject = new JSONObject(resultSet.getString("json_value"));
                String jsonObject = resultSet.getString("json_value");
                JSONObject positionJsonObject = new JSONObject(resultSet.getString("position"));
                Position position = new Position.Builder().fromJSON(positionJsonObject);
                //var schema = builder.fromJSON(jsonObject);
                output.add(new MappingWrapper(resultSet.getInt("id"), jsonObject, position));
            }
        }
        catch (Exception exception)
        {
            System.out.println(exception);
        }
        */

        return output;
    }

    public MappingWrapper find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                int schemaId = resultSet.getInt("schema_category_id");
                int databaseId = resultSet.getInt("database_id");
                Integer rootObjectId = Utils.getIntOrNull(resultSet.getInt("root_object_id"));
                Integer rootMorphismId = Utils.getIntOrNull(resultSet.getInt("root_morphism_id"));
                String jsonValue = resultSet.getString("json_value");

                output.set(new MappingWrapper(foundId, schemaId, databaseId, rootObjectId, rootMorphismId, jsonValue));
            }
        });
    }

}
