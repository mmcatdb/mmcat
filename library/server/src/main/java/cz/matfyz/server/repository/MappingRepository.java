package cz.matfyz.server.repository;

import static cz.matfyz.server.repository.utils.Utils.*;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.mapping.MappingWrapper;
import cz.matfyz.server.repository.utils.DatabaseWrapper;

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
        List<Signature> primaryKey,
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
                    logical_model.category_id
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
                output.set(MappingWrapper.fromJsonValue(id, logicalModelId, parsedJsonValue));
            }
        }, "Mapping", id);
    }

    public List<MappingWrapper> findAll(Id logicalModelId) {
        return db.getMultiple((connection, output) -> {
            final var statement = connection.prepareStatement("""
                SELECT
                    mapping.id,
                    mapping.json_value,
                    logical_model.category_id
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

                output.add(MappingWrapper.fromJsonValue(foundId, logicalModelId, parsedJsonValue));
            }
        });
    }

    public void save(MappingWrapper wrapper) {
        db.run(connection -> {
            final var statement = connection.prepareStatement("""
                INSERT INTO mapping (id, logical_model_id, json_value)
                VALUES (?, ?, ?::jsonb)
                ON CONFLICT (id) DO UPDATE SET
                    json_value = EXCLUDED.json_value;
                """);
            setId(statement, 1, wrapper.id());
            setId(statement, 2, wrapper.logicalModelId);
            statement.setString(3, jsonValueWriter.writeValueAsString(wrapper.toJsonValue()));
            executeChecked(statement);
        });
    }

}
