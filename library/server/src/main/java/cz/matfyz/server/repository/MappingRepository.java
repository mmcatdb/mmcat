package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.getId;
import static cz.matfyz.server.repository.utils.Utils.setId;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingInfo;
import cz.matfyz.server.entity.mapping.MappingInit;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.MappingRepository.MappingJsonValue;
import cz.matfyz.server.repository.utils.DatabaseWrapper;
import cz.matfyz.server.repository.utils.Utils;

import java.sql.Statement;
import java.util.List;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@Repository
public class MappingRepository {

    @Autowired
    private DatabaseWrapper db;

    @JsonDeserialize(using = MappingJsonValue.Deserializer.class)
    public record MappingJsonValue(
        Key rootObjectKey,
        Signature[] primaryKey,
        String kindName,
        ComplexProperty accessPath,
        Version version,
        Version categoryVersion
    ) {
        public String toJsonValue() throws JsonProcessingException {
            return Utils.toJson(this);
        }

        public static class Deserializer extends StdDeserializer<MappingJsonValue> {

            public Deserializer() {
                this(null);
            }

            public Deserializer(Class<?> vc) {
                super(vc);
            }

            @Override
            public MappingJsonValue deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                JsonNode node = parser.getCodec().readTree(parser);

                Key rootObjectKey = new ObjectMapper().treeToValue(node.get("rootObjectKey"), Key.class);
                Signature[] primaryKey = new ObjectMapper().treeToValue(node.get("primaryKey"), Signature[].class);
                String kindName = node.get("kindName").asText();
                ComplexProperty accessPath = new ObjectMapper().treeToValue(node.get("accessPath"), ComplexProperty.class);
                Version version = new ObjectMapper().treeToValue(node.get("version"), Version.class);
                Version categoryVersion = new ObjectMapper().treeToValue(node.get("categoryVersion"), Version.class);

                return new MappingJsonValue(rootObjectKey, primaryKey, kindName, accessPath, version, categoryVersion);
            }
         }
    }

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(MappingJsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(MappingJsonValue.class);

    public MappingWrapper find(Id id) {
        return db.get((connection, output) -> {
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
                //Id categoryId = getId(resultSet, "schema_category_id");
                final MappingJsonValue parsedJsonValue = jsonValueReader.readValue(jsonValue);
                output.set(new MappingWrapper(id, logicalModelId, parsedJsonValue));
            }
        }, "Mapping", id);
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
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
                //Id categoryId = getId(resultSet, "schema_category_id");
                final MappingJsonValue parsedJsonValue = jsonValueReader.readValue(jsonValue);

                output.add(new MappingWrapper(foundId, logicalModelId, parsedJsonValue));
            }
        });
    }

    public List<MappingInfo> findAllInfos(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
            var statement = connection.prepareStatement("""
                SELECT
                    mapping.id,
                    mapping.json_value::json->>'kindName' as kindName,
                    mapping.json_value::json->>'version' as version,
                    mapping.json_value::json->>'categoryVersion' as categoryVersion
                FROM mapping
                WHERE logical_model_id = ?
                ORDER BY id;
                """);
            setId(statement, 1, logicalModelId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Id foundId = getId(resultSet, "id");
                String kindName = resultSet.getString("kindName");
                Version version = new Version(resultSet.getString("version"));
                Version categoryVersion = new Version(resultSet.getString("categoryVersion"));

                output.add(new MappingInfo(foundId, kindName, version, categoryVersion));
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
            statement.setString(2, jsonValueWriter.writeValueAsString(init.toJsonValue()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
