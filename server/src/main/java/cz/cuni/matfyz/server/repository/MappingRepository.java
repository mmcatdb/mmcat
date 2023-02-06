package cz.cuni.matfyz.server.repository;

import static cz.cuni.matfyz.server.repository.utils.Utils.getId;
import static cz.cuni.matfyz.server.repository.utils.Utils.setId;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.mapping.MappingInfo;
import cz.cuni.matfyz.server.entity.mapping.MappingInit;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;
import cz.cuni.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
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
    private SchemaCategoryRepository categoryRepository;

    private record RawMappingWrapper(
        Id id,
        Id logicalModelId,
        Id categoryId,
        ParsedJsonValue parsedJsonValue
    ) {
        public MappingWrapper toMapping(SchemaObjectWrapper rootObject) {
            return new MappingWrapper(
                id,
                logicalModelId,
                rootObject,
                parsedJsonValue.primaryKey,
                parsedJsonValue.kindName,
                parsedJsonValue.accessPath
            );
        }
    }

    private record ParsedJsonValue(
        Key rootObjectKey,
        Signature[] primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(ParsedJsonValue.class);

    public MappingWrapper find(Id id) {
        final RawMappingWrapper rawMapping = db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    mapping.json_value,
                    mapping.logical_model_id,
                    logical_model.schema_category_id
                FROM mapping
                JOIN logical_model ON logical_model.id = mapping.logical_model_id
                WHERE mapping.id = ?;
                """);
            setId(statement, 1, id);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String jsonValue = resultSet.getString("json_value");
                Id logicalModelId = getId(resultSet, "logical_model_id");
                Id categoryId = getId(resultSet, "schema_category_id");
                final ParsedJsonValue parsedJsonValue = jsonValueReader.readValue(jsonValue);

                output.set(new RawMappingWrapper(id, logicalModelId, categoryId, parsedJsonValue));
            }
        }, "Mapping with id: %s not found.", id);

        return DatabaseWrapper.join(
            mapping -> mapping.toMapping(categoryRepository.findObject(mapping.categoryId, mapping.parsedJsonValue.rootObjectKey)),
            rawMapping
        );
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        List<RawMappingWrapper> rawMappings = db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    mapping.id,
                    mapping.json_value,
                    logical_model.schema_category_id
                FROM mapping
                JOIN logical_model ON logical_model.id = mapping.logical_model_id
                WHERE logical_model.id = ?
                ORDER BY mapping.id;
                """);
            setId(statement, 1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                String jsonValue = resultSet.getString("json_value");
                Id categoryId = getId(resultSet, "schema_category_id");
                final ParsedJsonValue parsedJsonValue = jsonValueReader.readValue(jsonValue);

                output.add(new RawMappingWrapper(foundId, logicalModelId, categoryId, parsedJsonValue));
            }
        });

        if (rawMappings.isEmpty())
            return List.of();

        final var categoryId = rawMappings.get(0).categoryId;
        final var objects = List.of(categoryRepository.find(categoryId).objects);

        return DatabaseWrapper.joinMultiple(
            (mapping, object) -> mapping.parsedJsonValue.rootObjectKey.equals(object.key()),
            (mapping, object) -> mapping.toMapping(object),
            rawMappings,
            objects,
            mapping -> "Root object with key: " + mapping.parsedJsonValue.rootObjectKey + " not found."
        );
    }

    public List<MappingInfo> findAllInfos(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    mapping.id,
                    mapping.json_value::json->>'kindName' as kindName
                FROM mapping
                WHERE logical_model_id = ?
                ORDER BY id;
                """);
            setId(statement, 1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                String jsonValue = resultSet.getString("kindName");

                output.add(new MappingInfo(foundId, jsonValue));
            }
        });
    }

    public Id add(MappingInit init) {
        return db.get((connection, output) -> {
            var statement = connection.prepareStatement("""
                INSERT INTO mapping (logical_model_id, json_value)
                VALUES (?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            setId(statement, 1, init.logicalModelId());
            statement.setString(2, init.toJsonValue());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
