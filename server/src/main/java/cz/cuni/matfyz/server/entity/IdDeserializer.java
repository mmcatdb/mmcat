package cz.cuni.matfyz.server.entity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class IdDeserializer extends StdDeserializer<Id> { 

    public IdDeserializer() { 
        this(null); 
    } 

    public IdDeserializer(Class<?> vc) { 
        super(vc); 
    }

    @Override
    public Id deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        return new Id(node.asText());
    }
}