package cz.matfyz.server.entity;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadata;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.schema.SchemaSerializer.SerializedSchema;
import cz.matfyz.evolution.Version;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class SchemaCategoryWrapper extends VersionedEntity {

    public final String label;
    /** The current version of the whole project. */
    public Version systemVersion;

    public SerializedSchema schema;
    public SerializedMetadata metadata;

    private SchemaCategoryWrapper(Id id, Version version, Version lastValid, String label, Version systemVersion, SerializedSchema schema, SerializedMetadata metadata) {
        super(id, version, lastValid);
        this.label = label;
        this.systemVersion = systemVersion;
        this.schema = schema;
        this.metadata = metadata;
    }

    public static SchemaCategoryWrapper createNew(String label, SchemaCategory category, MetadataCategory metadata) {
        final var newVersion = Version.generateInitial();
        return new SchemaCategoryWrapper(
            Id.createNew(),
            newVersion,
            newVersion,
            label,
            newVersion,
            SchemaSerializer.serialize(category),
            MetadataSerializer.serialize(metadata)
        );
    }

    public Version systemVersion() {
        return systemVersion;
    }

    public void update(Version version, SchemaCategory schema, MetadataCategory metadata) {
        updateVersion(version, systemVersion);
        systemVersion = version;

        this.schema = SchemaSerializer.serialize(schema);
        this.metadata = MetadataSerializer.serialize(metadata);
    }

    public SchemaCategory toSchemaCategory() {
        return SchemaSerializer.deserialize(schema);
    }

    public MetadataCategory toMetadataCategory(SchemaCategory schemaCategory) {
        return MetadataSerializer.deserialize(metadata, schemaCategory);
    }

    private record JsonValue(
        SerializedSchema schema,
        SerializedMetadata metadata
    ) implements Serializable {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static SchemaCategoryWrapper fromJsonValue(Id id, Version version, Version lastValid, String label, Version systemVersion, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new SchemaCategoryWrapper(
            id,
            version,
            lastValid,
            label,
            systemVersion,
            json.schema,
            json.metadata
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(schema, metadata));
    }

}
