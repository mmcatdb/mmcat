package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.builder.SchemaCategoryContext;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.repository.utils.Utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author jachym.bartik
 */
@JsonDeserialize(using = SchemaCategoryWrapper.Deserializer.class)
public class SchemaCategoryWrapper extends SchemaCategoryInfo {

    public final SchemaObjectWrapper[] objects;
    public final SchemaMorphismWrapper[] morphisms;

    private SchemaCategoryWrapper(Id id, String label, Version version, SchemaObjectWrapper[] objects, SchemaMorphismWrapper[] morphisms) {
        super(id, label, version);
        this.objects = objects;
        this.morphisms = morphisms;
    }

    private static final ObjectReader reader = new ObjectMapper().readerFor(SchemaCategoryWrapper.class);

    public static SchemaCategoryWrapper createNew(String label) {
        return new SchemaCategoryWrapper(
            null,
            label,
            Version.generateInitial(),
            new SchemaObjectWrapper[] {},
            new SchemaMorphismWrapper[] {}
        );
    }

    public static SchemaCategoryWrapper fromSchemaCategory(SchemaCategory category, SchemaCategoryContext context) {
        final var morphisms = category.allMorphisms().stream().map(SchemaMorphismWrapper::fromSchemaMorphism).toArray(SchemaMorphismWrapper[]::new);
        final var objects = category.allObjects().stream()
            .map(object -> SchemaObjectWrapper.fromSchemaObject(object, context))
            .toArray(SchemaObjectWrapper[]::new);

        return new SchemaCategoryWrapper(
            context.getId(),
            category.label,
            context.getVersion(),
            objects,
            morphisms
        );
    }

    public SchemaCategory toSchemaCategory(SchemaCategoryContext context) {
        context.setId(id);
        context.setVersion(version);

        final var category = new SchemaCategory(label);

        for (final var objectWrapper : objects)
            category.addObject(objectWrapper.toSchemaObject(context));

        for (final var morphismWrapper : morphisms)
            category.addMorphism(morphismWrapper.toSchemaMorphism(context));

        return category;
    }

    /**
     * Custom deserialization from the database.
     */
    public static SchemaCategoryWrapper fromJsonValue(Id id, String jsonValue) throws JsonProcessingException {
        return reader.withAttribute("id", id).readValue(jsonValue);
    }

    /**
     * Custom serialization for the database.
     */
    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "id");
    }

    public static class Deserializer extends StdDeserializer<SchemaCategoryWrapper> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader idJsonReader = new ObjectMapper().readerFor(Id.class);
        private static final ObjectReader versionJsonReader = new ObjectMapper().readerFor(Version.class);
        private static final ObjectReader objectsJsonReader = new ObjectMapper().readerFor(SchemaObjectWrapper[].class);
        private static final ObjectReader morphismsJsonReader = new ObjectMapper().readerFor(SchemaMorphismWrapper[].class);
    
        @Override
        public SchemaCategoryWrapper deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final var idFromContext = (Id) context.getAttribute("id");
            final Id id = idFromContext != null ? idFromContext : idJsonReader.readValue(node.get("id"));
            
            final var label = node.get("label").asText();
            final Version version = versionJsonReader.readValue(node.get("version"));

            final SchemaObjectWrapper[] objects = objectsJsonReader.readValue(node.get("objects"));
            final SchemaMorphismWrapper[] morphisms = morphismsJsonReader.readValue(node.get("morphisms"));
                        
            return new SchemaCategoryWrapper(id, label, version, objects, morphisms);
        }

    }
}
