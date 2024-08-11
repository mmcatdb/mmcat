package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class SchemaUpdate extends Entity {

    public final Id categoryId;
    public final Version prevVersion;
    public final Version nextVersion;
    public final List<VersionedSMO> operations;

    private SchemaUpdate(Id id, Id categoryId, Version prevVersion, Version nextVersion, List<VersionedSMO> operations) {
        super(id);
        this.categoryId = categoryId;
        this.prevVersion = prevVersion;
        this.nextVersion = nextVersion;
        this.operations = operations;
    }

    public static SchemaUpdate fromInit(SchemaUpdateInit init, Id categoryId, Version systemVersion) {
        final String newSchemaVersion = init.operations().getLast().version();

        return new SchemaUpdate(
            null,
            categoryId,
            init.prevVersion(),
            systemVersion.generateNext(newSchemaVersion),
            init.operations()
        );
    }

    public SchemaCategoryUpdate toEvolution() {
        return new SchemaCategoryUpdate(
            prevVersion,
            operations.stream().map(VersionedSMO::smo).toList()
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJson(new JsonValue(prevVersion, nextVersion, operations));
    }

    private record JsonValue(
        Version prevVersion,
        Version nextVersion,
        List<VersionedSMO> operations
    ) {}

    private static final ObjectReader reader = new ObjectMapper().readerFor(JsonValue.class);

    public static SchemaUpdate fromJsonValue(Id id, Id categoryId, String jsonValue) throws JsonProcessingException {
        final JsonValue parsedValue = reader.readValue(jsonValue);

        return new SchemaUpdate(
            id,
            categoryId,
            parsedValue.prevVersion,
            parsedValue.nextVersion,
            parsedValue.operations
        );
    }

}
