package cz.matfyz.server.entity.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.builder.MetadataContext;
import cz.matfyz.server.entity.Id;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SchemaCategoryWrapper extends SchemaCategoryInfo {

    public final SchemaObjectWrapper[] objects;
    public final SchemaMorphismWrapper[] morphisms;

    private SchemaCategoryWrapper(Id id, String label, Version version, Version systemVersion, SchemaObjectWrapper[] objects, SchemaMorphismWrapper[] morphisms) {
        super(id, label, version, systemVersion);
        this.objects = objects;
        this.morphisms = morphisms;
    }

    public static SchemaCategoryWrapper fromSchemaCategory(SchemaCategory category, MetadataContext context) {
        final var morphisms = category.allMorphisms().stream().map(SchemaMorphismWrapper::fromSchemaMorphism).toArray(SchemaMorphismWrapper[]::new);
        final var objects = category.allObjects().stream()
            .map(object -> SchemaObjectWrapper.fromSchemaObject(object, context))
            .toArray(SchemaObjectWrapper[]::new);

        return new SchemaCategoryWrapper(
            context.getId(),
            category.label,
            context.getVersion(),
            context.getSystemVersion(),
            objects,
            morphisms
        );
    }

    public SchemaCategory toSchemaCategory() {
        return toSchemaCategory(null);
    }

    public SchemaCategory toSchemaCategory(@Nullable MetadataContext context) {
        if (context != null) {
            context.setId(id());
            context.setVersion(version);
        }

        final var category = new SchemaCategory(label);

        for (final var objectWrapper : objects)
            category.addObject(objectWrapper.toSchemaObject(context));

        for (final var morphismWrapper : morphisms) {
            final var disconnectedMorphism = morphismWrapper.toDisconnectedSchemaMorphism();
            category.addMorphism(disconnectedMorphism.toSchemaMorphism(category::getObject));
        }

        return category;
    }

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(SerializedSchemaCategory.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(SerializedSchemaCategory.class);

    public static SchemaCategoryWrapper fromJsonValue(Id id, String jsonString) throws JsonProcessingException {
        final SerializedSchemaCategory jsonValue = jsonValueReader.readValue(jsonString);
        return new SchemaCategoryWrapper(id, jsonValue.label, jsonValue.version, jsonValue.systemVersion, jsonValue.objects, jsonValue.morphisms);
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(toSerializedSchemaCategory());
    }

    public SerializedSchemaCategory toSerializedSchemaCategory() {
        return new SerializedSchemaCategory(label, version, systemVersion, objects, morphisms);
    }

    // TODO this is highly suboptimal, refactor when possible

    public record SerializedSchemaCategory(
        String label,
        Version version,
        Version systemVersion,
        SchemaObjectWrapper[] objects,
        SchemaMorphismWrapper[] morphisms
    ) implements Serializable {

        public SchemaCategory toSchemaCategory() {
            final var wrapper = new SchemaCategoryWrapper(null, label, version, systemVersion, objects, morphisms);
            return wrapper.toSchemaCategory();
        }

    }

}
