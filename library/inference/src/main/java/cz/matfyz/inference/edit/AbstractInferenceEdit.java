package cz.matfyz.inference.edit;

import cz.matfyz.core.schema.SchemaCategory;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ReferenceMergeInferenceEdit.class, name = "reference")
})
@JsonDeserialize(using = AbstractInferenceEdit.Deserializer.class)
public abstract class AbstractInferenceEdit {

    public abstract SchemaCategory applyEdit(SchemaCategory schemaCategory);

    public static class Deserializer extends StdDeserializer<AbstractInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public AbstractInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            if (node.has("type")) {
                String type = node.get("type").asText();
                switch (type) {
                    case "reference":
                        return (AbstractInferenceEdit) parser.getCodec().treeToValue(node, ReferenceMergeInferenceEdit.class);
                    default:
                        throw new IllegalArgumentException("Unknown type for AbstractInferenceEdit");
                }
            }
            throw new IllegalArgumentException("Missing type field for AbstractInferenceEdit");
        }
    }
}

