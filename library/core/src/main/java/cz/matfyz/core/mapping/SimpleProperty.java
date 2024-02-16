package cz.matfyz.core.mapping;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A simple value node in the access path tree. Its context is undefined (null).
 * @author jachymb.bartik
 */
@JsonSerialize(using = SimpleProperty.Serializer.class)
@JsonDeserialize(using = SimpleProperty.Deserializer.class)
public class SimpleProperty extends AccessPath {

    public SimpleProperty(Name name, Signature signature) {
        super(name, signature);
    }
    
    public SimpleProperty(String name, Signature signature) {
        this(new StaticName(name), signature);
    }
    
    public SimpleProperty(Signature name, Signature signature) {
        this(new DynamicName(name), signature);
    }
    
    @Override protected boolean hasSignature(Signature signature) {
        return this.signature.equals(signature);
    }

    @Override protected List<AccessPath> getPropertyPathInternal(Signature signature) {
        return this.signature.contains(signature)
            ? new ArrayList<>(List.of(this))
            : null;
    }

    @Override public AccessPath tryGetSubpathForObject(Key key, SchemaCategory schema) {
        final SchemaMorphism morphism = schema.getMorphism(signature);

        return morphism.dom().key().equals(key) ? this : null;
    }
    
    @Override public void printTo(Printer printer) {
        printer.append(name).append(": ").append(signature);
    }

    @Override public String toString() {
        return Printer.print(this);
    }
    
    public static class Serializer extends StdSerializer<SimpleProperty> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SimpleProperty> t) {
            super(t);
        }

        @Override public void serialize(SimpleProperty property, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("name", property.name);
            generator.writePOJOField("signature", property.signature);
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<SimpleProperty> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader nameJsonReader = new ObjectMapper().readerFor(Name.class);
        private static final ObjectReader signatureJsonReader = new ObjectMapper().readerFor(Signature.class);
    
        @Override public SimpleProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Name name = nameJsonReader.readValue(node.get("name"));
            final Signature signature = signatureJsonReader.readValue(node.get("signature"));

            return new SimpleProperty(name, signature);
        }

    }

}
