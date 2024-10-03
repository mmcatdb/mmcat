package cz.matfyz.server.entity.schema;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadata;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.schema.SchemaSerializer.SerializedSchema;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SchemaCategoryWrapper extends SchemaCategoryInfo {

    public final SerializedSchema schema;
    public final SerializedMetadata metadata;

    private SchemaCategoryWrapper(Id id, String label, Version version, Version systemVersion, SerializedSchema schema, SerializedMetadata metadata) {
        super(id, label, version, systemVersion);
        this.schema = schema;
        this.metadata = metadata;
    }

    public static SchemaCategoryWrapper createNew(String label, Version version, Version systemVersion, SchemaCategory category, MetadataCategory metadata) {
        return new SchemaCategoryWrapper(
            Id.createNew(),
            label,
            version,
            systemVersion,
            SchemaSerializer.serialize(category),
            MetadataSerializer.serialize(metadata)
        );
    }

    public static SchemaCategoryWrapper fromSchemaCategory(@Nullable Id id, String label, Version version, Version systemVersion, SchemaCategory category, MetadataCategory metadata) {
        return new SchemaCategoryWrapper(
            id,
            label,
            version,
            systemVersion,
            SchemaSerializer.serialize(category),
            MetadataSerializer.serialize(metadata)
        );
    }

    public SchemaCategory toSchemaCategory() {
        return SchemaSerializer.deserialize(schema);
    }

    public MetadataCategory toMetadataCategory(SchemaCategory schemaCategory) {
        return MetadataSerializer.deserialize(metadata, schemaCategory);
    }

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static SchemaCategoryWrapper fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new SchemaCategoryWrapper(id, json.label, json.version, json.systemVersion, json.schema, json.metadata);
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(label, version, systemVersion, schema, metadata));
    }

    private record JsonValue(
        String label,
        Version version,
        Version systemVersion,
        SerializedSchema schema,
        SerializedMetadata metadata
    ) implements Serializable {}

}
