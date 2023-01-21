package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author jachym.bartik
 */
@Repository
public class MappingRepository {

    @Autowired
    private DatabaseWrapper db;

    @Autowired
    private SchemaObjectRepository objectRepository;

    private record RawMappingWrapper(
        Id id,
        Id logicalModelId,
        Id rootObjectId,
        String jsonValue
    ) {
        public MappingWrapper toMapping(SchemaObjectWrapper object) {
            return new MappingWrapper(id, logicalModelId, object, jsonValue);
        }
    }

    public MappingWrapper find(Id id) {
        final RawMappingWrapper rawMapping = db.get((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE id = ?;");
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                Id logicalModelId = getId(resultSet, "logical_model_id");
                Id rootObjectId = getId(resultSet, "root_object_id");
                String jsonValue = resultSet.getString("json_value");

                output.set(new RawMappingWrapper(foundId, logicalModelId, rootObjectId, jsonValue));
            }
        }, "Mapping with id: %s not found.", id);

        return DatabaseWrapper.join(
            mapping -> mapping.toMapping(objectRepository.find(mapping.rootObjectId)),
            rawMapping
        );
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        List<RawMappingWrapper> rawMappings = db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE logical_model_id = ? ORDER BY id;");
            setId(statement, 1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                Id rootObjectId = getId(resultSet, "root_object_id");
                String jsonValue = resultSet.getString("json_value");

                output.add(new RawMappingWrapper(foundId, logicalModelId, rootObjectId, jsonValue));
            }
        });

        final var objects = objectRepository.findAllInLogicalModel(logicalModelId);
        return DatabaseWrapper.joinMultiple(
            (mapping, rootObject) -> mapping.rootObjectId.equals(rootObject.id),
            (mapping, rootObject) -> mapping.toMapping(rootObject),
            rawMappings,
            objects,
            mapping -> "Root object with id: " + mapping.rootObjectId + " not found."
        );
    }

    public List<MappingInfo> findAllInfos(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("SELECT * FROM mapping WHERE logical_model_id = ? ORDER BY id;");
            setId(statement, 1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");

                output.add(new MappingInfo(foundId, jsonValue));
            }
        });
    }

    public Id add(MappingInit mapping) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO mapping (logical_model_id, root_object_id, json_value)
                VALUES (?, ?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            setId(statement, 1, mapping.logicalModelId());
            setId(statement, 2, mapping.rootObjectId());
            statement.setString(3, mapping.jsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
