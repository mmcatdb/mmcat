package cz.matfyz.server.category;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadata;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.schema.SchemaSerializer.SerializedSchema;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.utils.entity.VersionedEntity;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SchemaCategoryEntity extends VersionedEntity {

    /**
     * If this category was created as an example, this is its type. Useful when we want to, e.g., customize queries for the example.
     * It's a string instead of an enum to allow dynamic variants and also to support previously created examples that might be no longer available.
     */
    public final @Nullable String example;
    /** The current version of the whole project. */
    public Version systemVersion;
    public String label;
    public SerializedSchema schema;
    public SerializedMetadata metadata;

    private SchemaCategoryEntity(Id id, Version version, Version lastValid, @Nullable String example) {
        super(id, version, lastValid);
        this.example = example;
    }

    public static SchemaCategoryEntity createNew(@Nullable String example, String label, SchemaCategory category, MetadataCategory metadata) {
        final var newVersion = Version.generateInitial();
        final var output = new SchemaCategoryEntity(Id.createNew(), newVersion, newVersion, example);
        output.systemVersion = newVersion;
        output.label = label;
        output.schema = SchemaSerializer.serialize(category);
        output.metadata = MetadataSerializer.serialize(metadata);
        return output;
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

    public static SchemaCategoryEntity fromJsonValue(Id id, Version version, Version lastValid, @Nullable String example, Version systemVersion, String label, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        final var output = new SchemaCategoryEntity(id, version, lastValid, example);
        output.systemVersion = systemVersion;
        output.label = label;
        output.schema = json.schema;
        output.metadata = json.metadata;
        return output;
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(schema, metadata));
    }

}
