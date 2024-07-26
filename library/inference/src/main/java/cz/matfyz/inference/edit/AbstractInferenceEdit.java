package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.inference.edit.utils.InferenceEditorUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @JsonSubTypes.Type(value = ReferenceMergeInferenceEdit.class, name = "reference"),
    @JsonSubTypes.Type(value = PrimaryKeyMergeInferenceEdit.class, name = "primaryKey"),
    @JsonSubTypes.Type(value = ClusterInferenceEdit.class, name = "cluster"),
    @JsonSubTypes.Type(value = RecursionInferenceEdit.class, name = "recursion")
})
@JsonDeserialize(using = AbstractInferenceEdit.Deserializer.class)
public abstract class AbstractInferenceEdit {

    protected SchemaCategory oldSchemaCategory;
    protected SchemaCategory newSchemaCategory;

    protected Set<Signature> signaturesToDelete = new HashSet<>();
    protected Set<Key> keysToDelete = new HashSet<>();

    protected void setSchemaCategories(SchemaCategory schemaCategory) {
        this.oldSchemaCategory = schemaCategory;
        this.newSchemaCategory = InferenceEditorUtils.createSchemaCategoryCopy(schemaCategory);
    }

    public abstract SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory);
    public abstract List<Mapping> applyMappingEdit(List<Mapping> mappings);

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
                    case "primaryKey":
                        return (AbstractInferenceEdit) parser.getCodec().treeToValue(node, PrimaryKeyMergeInferenceEdit.class);
                    case "cluster":
                        return (AbstractInferenceEdit) parser.getCodec().treeToValue(node, ClusterInferenceEdit.class);
                    case "recursion":
                        return (AbstractInferenceEdit) parser.getCodec().treeToValue(node, RecursionInferenceEdit.class);
                    default:
                        throw new IllegalArgumentException("Unknown type for AbstractInferenceEdit");
                }
            }
            throw new IllegalArgumentException("Missing type field for AbstractInferenceEdit");
        }
    }
}

