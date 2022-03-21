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
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE schema_category_id = ?;");
            statement.setInt(1, categoryId);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                int databaseId = resultSet.getInt("database_id");
                Integer rootObjectId = Utils.getIntOrNull(resultSet.getInt("root_object_id"));
                Integer rootMorphismId = Utils.getIntOrNull(resultSet.getInt("root_morphism_id"));
                String mappingJsonValue = resultSet.getString("mapping_json_value");
                String jsonValue = resultSet.getString("json_value");

                output.add(new MappingWrapper(foundId, databaseId, categoryId, rootObjectId, rootMorphismId, mappingJsonValue, jsonValue));
            }
        });
    }

    public MappingWrapper find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                int categoryId = resultSet.getInt("schema_category_id");
                int databaseId = resultSet.getInt("database_id");
                Integer rootObjectId = Utils.getIntOrNull(resultSet.getInt("root_object_id"));
                Integer rootMorphismId = Utils.getIntOrNull(resultSet.getInt("root_morphism_id"));
                String mappingJsonValue = resultSet.getString("mapping_json_value");
                String jsonValue = resultSet.getString("json_value");

                output.set(new MappingWrapper(foundId, databaseId, categoryId, rootObjectId, rootMorphismId, mappingJsonValue, jsonValue));
            }
        });
    }

}
