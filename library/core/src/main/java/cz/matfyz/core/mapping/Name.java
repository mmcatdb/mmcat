package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Signature;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.checkerframework.checker.nullness.qual.Nullable;

@JsonDeserialize(using = Name.Deserializer.class)
public abstract class Name implements Serializable {

    protected Name() {}

    public static class Deserializer extends StdDeserializer<Name> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader signatureJsonReader = new ObjectMapper().readerFor(Signature.class);

        @Override public Name deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            if (node.has("value"))
                return new StaticName(node.get("value").asText());
            if (node.has("type"))
                return new SpecialName(node.get("type").asText());

            final Signature signature = signatureJsonReader.readValue(node.get("signature"));
            final @Nullable String pattern = node.hasNonNull("pattern") ? node.get("pattern").asText() : null;

            return new DynamicName(signature, pattern);
        }

    }

}
