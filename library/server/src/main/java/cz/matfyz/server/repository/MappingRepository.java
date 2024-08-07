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
import cz.matfyz.server.repository.utils.DatabaseWrapper;

import java.sql.Statement;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MappingRepository {

    @Autowired
    private DatabaseWrapper db;

    public record MappingJsonValue(
        Key rootObjectKey,
        Signature[] primaryKey,
        String kindName,
        ComplexProperty accessPath,
        Version version
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(MappingJsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(MappingJsonValue.class);

    public MappingWrapper find(Id id) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    mapping.json_value,
                    mapping.logical_model_id,
                    logical_model.schema_category_id
                FROM mapping
                JOIN logical_model ON logical_model.id = mapping.logical_model_id
                WHERE mapping.id = ?;
                """);
            setId(statement, 1, id);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                final String jsonValue = resultSet.getString("json_value");
                final Id logicalModelId = getId(resultSet, "logical_model_id");
                final MappingJsonValue parsedJsonValue = jsonValueReader.readValue(jsonValue);
                output.set(new MappingWrapper(id, logicalModelId, parsedJsonValue));
            }
        }, "Mapping", id);
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
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
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id foundId = getId(resultSet, "id");
                final String jsonValue = resultSet.getString("json_value");
                final MappingJsonValue parsedJsonValue = jsonValueReader.readValue(jsonValue);

                output.add(new MappingWrapper(foundId, logicalModelId, parsedJsonValue));
            }
        });
    }

    public List<MappingInfo> findAllInfos(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    mapping.id,
                    mapping.json_value::json->>'kindName' as kindName,
                    mapping.json_value::json->>'version' as version
                FROM mapping
                WHERE logical_model_id = ?
                ORDER BY id;
                """);
            setId(statement, 1, logicalModelId);
            final var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                final Id foundId = getId(resultSet, "id");
                final String kindName = resultSet.getString("kindName");
                Version version = Version.fromString(resultSet.getString("version"));

                output.add(new MappingInfo(foundId, kindName, version));
            }
        });
    }

    public Id add(MappingInit init) {
        return db.get((connection, output) -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO mapping (logical_model_id, json_value)
                VALUES (?, ?::jsonb);
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            setId(statement, 1, init.logicalModelId());
            // TODO Fix this.
            statement.setString(2, jsonValueWriter.writeValueAsString(init.toJsonValue(Version.generateInitial(null))));

            final int affectedRows = statement.executeUpdate();
            if (affectedRows == 0)
                return;

            final var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next())
                output.set(getId(generatedKeys, "id"));
        });
    }

}
