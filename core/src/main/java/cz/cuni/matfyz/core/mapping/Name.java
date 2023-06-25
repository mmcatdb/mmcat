package cz.cuni.matfyz.core.mapping;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author pavel.koupil, jachym.bartik
 */
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

        private static final ObjectReader staticNameJsonReader = new ObjectMapper().readerFor(StaticName.class);
        private static final ObjectReader dynamicNameJsonReader = new ObjectMapper().readerFor(DynamicName.class);
    
        @Override
        public Name deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            return node.has("signature")
                ? dynamicNameJsonReader.readValue(node)
                : staticNameJsonReader.readValue(node);
        }

    }

}
