package cz.cuni.matfyz.server.entity.evolution;

import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.builder.SchemaCategoryContext;
import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.repository.utils.Utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * @author jachym.bartik
 */
public class SchemaUpdate extends Entity {

    public final Id categoryId;
    public final Version prevVersion;
    public Version nextVersion;
    public final List<VersionedSMO> operations;

    private SchemaUpdate(Id id, Id categoryId, Version prevVersion, Version nextVersion, List<VersionedSMO> operations) {
        super(id);
        this.categoryId = categoryId;
        this.prevVersion = prevVersion;
        this.nextVersion = nextVersion;
        this.operations = operations;
    }

    public static SchemaUpdate fromInit(SchemaUpdateInit init, Id categoryId) {
        return new SchemaUpdate(
            null,
            categoryId,
            init.prevVersion(),
            null,
            init.operations()
        );
    }

    public cz.cuni.matfyz.evolution.schema.SchemaCategoryUpdate toEvolution(SchemaCategoryContext context) {
        return new cz.cuni.matfyz.evolution.schema.SchemaCategoryUpdate(
            prevVersion,
            operations.stream().map(operation -> operation.smo().toEvolution(context)).toList()
        );
    }

    private static final List<String> idPropertyNames = List.of("id", "categoryId");

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, idPropertyNames);
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
