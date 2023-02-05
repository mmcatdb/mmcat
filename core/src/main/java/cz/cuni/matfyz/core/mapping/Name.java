package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.FromJSONSwitchBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;

import java.io.IOException;
import java.util.Set;

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
public abstract class Name implements JSONConvertible {

    protected Name() {}

    public static class Builder extends FromJSONSwitchBuilderBase<Name> {

        @Override
        protected Set<FromJSONBuilderBase<? extends Name>> getChildConverters() {
            return Set.of(
                new StaticName.Builder(),
                new DynamicName.Builder()
            );
        }
        
    }

    public static class Deserializer extends StdDeserializer<Name> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static ObjectReader staticNameJSONReader = new ObjectMapper().readerFor(StaticName.class);
        private static ObjectReader dynamicNameJSONReader = new ObjectMapper().readerFor(DynamicName.class);
    
        @Override
        public Name deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            return node.has("signature")
                ? dynamicNameJSONReader.readValue(node)
                : staticNameJSONReader.readValue(node);
        }

    }

}
