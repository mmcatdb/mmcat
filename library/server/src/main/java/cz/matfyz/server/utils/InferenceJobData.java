package cz.matfyz.server.utils;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.repository.utils.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/*
 * This is a helper class for storing intermediate results of the inferencejob
 */
@JsonDeserialize(using = InferenceJobData.Deserializer.class)
public class InferenceJobData implements Serializable {

    @JsonDeserialize(using = InferenceData.Deserializer.class)
    public static class InferenceData implements Serializable {
        public final SchemaCategoryWrapper schemaCategory;
        public final Mapping mapping;

        public InferenceData(SchemaCategoryWrapper schemaCategory, Mapping mapping) {
            this.schemaCategory = schemaCategory;
            this.mapping = mapping;
        }

        public static class Deserializer extends StdDeserializer<InferenceData> {
            private static final ObjectReader schemaCategoryReader = new ObjectMapper().readerFor(SchemaCategoryWrapper.class);
            private static final ObjectReader mappingReader = new ObjectMapper().readerFor(Mapping.class);

            public Deserializer() {
                this(null);
            }

            public Deserializer(Class<?> vc) {
                super(vc);
            }

            @Override
            public InferenceData deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                final JsonNode node = parser.getCodec().readTree(parser);

                if (node == null || node.isNull()) {
                    throw new IllegalArgumentException("InferenceData JSON is null");
                }

                final SchemaCategoryWrapper schemaCategory = node.has("schemaCategory") && !node.get("schemaCategory").isNull() ?
                    schemaCategoryReader.readValue(node.get("schemaCategory")) : null;
                final Mapping mapping = node.has("mapping") && !node.get("mapping").isNull() && node.get("mapping").size() > 0 ?
                    mappingReader.readValue(node.get("mapping")) : null;

                return new InferenceData(schemaCategory, mapping);
            }
        }
    }

    public final InferenceData inference;
    public List<String> manual; // TODO: trying just with string for now
    public SchemaCategoryWrapper finalSchema;

    public InferenceJobData(InferenceData inference) {
        this.inference = inference;
        this.manual = new ArrayList<>();
    }

    public InferenceJobData(InferenceData inference, List<String> manual, SchemaCategoryWrapper finalSchema) {
        this.inference = inference;
        this.manual = manual;
        this.finalSchema = finalSchema;
    }

    /***
     * Custom serialization
     */
    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, "id");
    }

    public static class Deserializer extends StdDeserializer<InferenceJobData> {
        private static final ObjectReader inferenceDataReader = new ObjectMapper().readerFor(InferenceData.class);
        private static final ObjectReader schemaCategoryReader = new ObjectMapper().readerFor(SchemaCategoryWrapper.class);

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public InferenceJobData deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            if (node == null || node.isNull()) {
                throw new IllegalArgumentException("InferenceJobData JSON is null");
            }

            final InferenceData inference = node.has("inference") && !node.get("inference").isNull() ?
                inferenceDataReader.readValue(node.get("inference")) : null;
            final List<String> manual = new ArrayList<>();
            if (node.has("manual") && !node.get("manual").isNull()) {
                for (JsonNode item : node.get("manual")) {
                    manual.add(item.asText());
                }
            }
            final SchemaCategoryWrapper finalSchema = node.has("finalSchema") && !node.get("finalSchema").isNull() ?
                schemaCategoryReader.readValue(node.get("finalSchema")) : null;

            return new InferenceJobData(inference, manual, finalSchema);
        }
    }

}
