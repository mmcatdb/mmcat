package cz.matfyz.inference.edit.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = PatternSegment.Deserializer.class)
public class PatternSegment {
    public final String nodeName;
    public final String direction;

    public PatternSegment(String nodeName, String direction) {
        this.nodeName = nodeName;
        this.direction = direction;
    }
    
    @Override
    public String toString() {
        return nodeName + " " + direction;
    }

    public static class Deserializer extends StdDeserializer<PatternSegment> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

         @Override
        public PatternSegment deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);

            String nodeName = node.get("nodeName").asText();
            String direction = node.get("direction").asText();

            return new PatternSegment(nodeName, direction);
        }
    }
}



