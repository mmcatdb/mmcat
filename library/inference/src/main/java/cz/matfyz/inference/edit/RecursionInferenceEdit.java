package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.edit.utils.PatternSegment;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = RecursionInferenceEdit.Deserializer.class)
public class RecursionInferenceEdit extends AbstractInferenceEdit {

    @JsonProperty("type")
    private final String type = "recursion";

    public final List<PatternSegment> pattern;

    public RecursionInferenceEdit(List<PatternSegment> pattern) {
        this.pattern = pattern;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySchemaCategoryEdit'");
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applyMappingEdit'");
    }

    public static class Deserializer extends StdDeserializer<RecursionInferenceEdit> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public RecursionInferenceEdit deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            List<PatternSegment> pattern = mapper.readValue(
                node.get("pattern").traverse(mapper),
                mapper.getTypeFactory().constructCollectionType(List.class, PatternSegment.class)
            );

            return new RecursionInferenceEdit(pattern);
        }
    }
}
