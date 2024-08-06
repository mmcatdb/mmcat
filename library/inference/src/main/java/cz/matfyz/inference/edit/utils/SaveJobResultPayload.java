package cz.matfyz.inference.edit.utils;

import cz.matfyz.inference.edit.AbstractInferenceEdit;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = SaveJobResultPayload.Deserializer.class)
public class SaveJobResultPayload {
    public final boolean permanent;
    public final AbstractInferenceEdit edit;


    public SaveJobResultPayload(boolean permanent, AbstractInferenceEdit edit) {
        this.permanent = permanent;
        this.edit = edit;
    }

        public static class Deserializer extends StdDeserializer<SaveJobResultPayload> {
        private static final ObjectReader abstractInferenceEditReader = new ObjectMapper().readerFor(AbstractInferenceEdit.class);

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public SaveJobResultPayload deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            if (node == null || node.isNull()) {
                throw new IllegalArgumentException("SaveJobResultPayload JSON is null");
            }

            final boolean permanent = node.has("permanent") && !node.get("permanent").isNull() ?
                    node.get("permanent").asBoolean() : false;

            final AbstractInferenceEdit edit = node.has("edit") && !node.get("edit").isNull() ?
                    abstractInferenceEditReader.readValue(node.get("edit")) : null;

            return new SaveJobResultPayload(permanent, edit);
        }
    }
}
