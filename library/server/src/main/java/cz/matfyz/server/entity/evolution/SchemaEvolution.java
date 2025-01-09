package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.category.SMO;
import cz.matfyz.evolution.category.SchemaEvolutionAlgorithm;
import cz.matfyz.evolution.metadata.MMO;
import cz.matfyz.evolution.metadata.MetadataEvolutionAlgorithm;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;
import cz.matfyz.server.service.SchemaCategoryService.SchemaEvolutionInit;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class SchemaEvolution extends Evolution {

    public final List<SMO> schema;
    public final List<MMO> metadata;

    private SchemaEvolution(Id id, Id categoryId, Version version, List<SMO> schema, List<MMO> metadata) {
        super(id, categoryId, version, EvolutionType.schema);
        this.schema = schema;
        this.metadata = metadata;
    }

    public static SchemaEvolution createFromInit(Id categoryId, Version version, SchemaEvolutionInit init) {
        return new SchemaEvolution(
            Id.createNew(),
            categoryId,
            version,
            init.schema(),
            init.metadata()
        );
    }

    public SchemaEvolutionAlgorithm toSchemaAlgorithm(Version prevVersion) {
        return new SchemaEvolutionAlgorithm(
            prevVersion,
            schema
        );
    }

    public MetadataEvolutionAlgorithm toMetadataAlgorithm(Version prevVersion) {
        return new MetadataEvolutionAlgorithm(
            prevVersion,
            metadata
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJson(new JsonValue(schema, metadata));
    }

    private record JsonValue(
        List<SMO> schema,
        List<MMO> metadata
    ) {}

    private static final ObjectReader reader = new ObjectMapper().readerFor(JsonValue.class);

    public static SchemaEvolution fromJsonValue(Id id, Id categoryId, Version version, String jsonValue) throws JsonProcessingException {
        final JsonValue parsedValue = reader.readValue(jsonValue);

        return new SchemaEvolution(
            id,
            categoryId,
            version,
            parsedValue.schema,
            parsedValue.metadata
        );
    }

}
