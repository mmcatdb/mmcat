package cz.cuni.matfyz.server.repository;

import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;
import cz.cuni.matfyz.server.repository.utils.Utils;

import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class MappingRepository {

    public MappingWrapper find(int id) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE id = ?;");
            statement.setInt(1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                int logicalModelId = resultSet.getInt("logical_model_id");
                Integer rootObjectId = Utils.getIntOrNull(resultSet.getInt("root_object_id"));
                Integer rootMorphismId = Utils.getIntOrNull(resultSet.getInt("root_morphism_id"));
                String mappingJsonValue = resultSet.getString("mapping_json_value");
                String jsonValue = resultSet.getString("json_value");

                output.set(new MappingWrapper(foundId, logicalModelId, rootObjectId, rootMorphismId, mappingJsonValue, jsonValue));
            }
        });
    }

    public List<MappingWrapper> findAll(int logicalModelId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE logical_model_id = ? ORDER BY id;");
            statement.setInt(1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                Integer rootObjectId = Utils.getIntOrNull(resultSet.getInt("root_object_id"));
                Integer rootMorphismId = Utils.getIntOrNull(resultSet.getInt("root_morphism_id"));
                String mappingJsonValue = resultSet.getString("mapping_json_value");
                String jsonValue = resultSet.getString("json_value");

                output.add(new MappingWrapper(foundId, logicalModelId, rootObjectId, rootMorphismId, mappingJsonValue, jsonValue));
            }
        });
    }

    public List<MappingInfo> findAllInfos(int logicalModelId) {
        return DatabaseWrapper.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE logical_model_id = ? ORDER BY id;");
            statement.setInt(1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int foundId = resultSet.getInt("id");
                String jsonValue = resultSet.getString("json_value");

                output.add(new MappingInfo(foundId, jsonValue));
            }
        });
    }

    public Integer add(MappingInit mapping) {
        return DatabaseWrapper.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO mapping (logical_model_id, root_object_id, root_morphism_id, mapping_json_value, json_value)
                VALUES (?, ?, ?, ?::jsonb, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setInt(1, mapping.logicalModelId());
            statement.setObject(2, mapping.rootObjectId(), Types.INTEGER); // The inserted value can be null.
            statement.setObject(3, mapping.rootMorphismId(), Types.INTEGER); // Same here.
            statement.setString(4, mapping.mappingJsonValue());
            statement.setString(5, mapping.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(generatedKeys.getInt("id"));
        });
    }

}
