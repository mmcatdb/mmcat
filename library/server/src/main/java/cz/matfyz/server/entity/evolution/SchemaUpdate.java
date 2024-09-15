package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.metadata.MMO;
import cz.matfyz.evolution.schema.SchemaCategoryUpdate;
import cz.matfyz.evolution.schema.SMO;
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
    public final List<SMO> schema;
    public final List<MMO> metadata;

    private SchemaUpdate(Id id, Id categoryId, Version prevVersion, Version nextVersion, List<SMO> schema, List<MMO> metadata) {
        super(id);
        this.categoryId = categoryId;
        this.prevVersion = prevVersion;
        this.nextVersion = nextVersion;
        this.schema = schema;
        this.metadata = metadata;
    }

    public static SchemaUpdate fromInit(SchemaUpdateInit init, Id categoryId, Version systemVersion) {
        return new SchemaUpdate(
            null,
            categoryId,
            init.prevVersion(),
            systemVersion.generateNext(null),
            init.schema(),
            init.metadata()
        );
    }

    public SchemaCategoryUpdate toEvolution() {
        return new SchemaCategoryUpdate(
            prevVersion,
            schema
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJson(new JsonValue(prevVersion, nextVersion, schema, metadata));
    }

    private record JsonValue(
        Version prevVersion,
        Version nextVersion,
        List<SMO> schema,
        List<MMO> metadata
    ) {}

    private static final ObjectReader reader = new ObjectMapper().readerFor(JsonValue.class);

    public static SchemaUpdate fromJsonValue(Id id, Id categoryId, String jsonValue) throws JsonProcessingException {
        final JsonValue parsedValue = reader.readValue(jsonValue);

        return new SchemaUpdate(
            id,
            categoryId,
            parsedValue.prevVersion,
            parsedValue.nextVersion,
            parsedValue.schema,
            parsedValue.metadata
        );
    }

}
