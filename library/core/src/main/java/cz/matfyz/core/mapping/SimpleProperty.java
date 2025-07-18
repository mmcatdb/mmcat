package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty.DynamicNameReplacement;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.utils.printable.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A simple value node in the access path tree. Its context is undefined (null).
 */
@JsonSerialize(using = SimpleProperty.Serializer.class)
@JsonDeserialize(using = SimpleProperty.Deserializer.class)
public class SimpleProperty extends AccessPath {

    public SimpleProperty(Name name, Signature signature) {
        super(name, signature);
    }

    @Override protected @Nullable List<AccessPath> getPropertyPathInternal(Signature signature) {
        return signature.isEmpty()
            ? new ArrayList<>(List.of(this))
            : null;
    }

    @Override public @Nullable AccessPath tryGetSubpathForObjex(Key key, SchemaCategory schema) {
        final SchemaMorphism morphism = schema.getMorphism(signature);

        return morphism.dom().key().equals(key) ? this : null;
    }

    @Override protected SimpleProperty copyForReplacement(Name name, Signature signature, @Nullable Map<DynamicName, DynamicNameReplacement> replacedNames) {
        return new SimpleProperty(name, signature);
    }

    @Override public void printTo(Printer printer) {
        printer.append(name).append(": ").append(signature);
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<SimpleProperty> {
        public Serializer() { this(null); }
        public Serializer(Class<SimpleProperty> t) { super(t); }

        @Override public void serialize(SimpleProperty property, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("name", property.name);
            generator.writePOJOField("signature", property.signature);
            generator.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<SimpleProperty> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public SimpleProperty deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);

            final Name name = codec.treeToValue(node.get("name"), Name.class);
            final Signature signature = codec.treeToValue(node.get("signature"), Signature.class);

            return new SimpleProperty(name, signature);
        }
    }

    // #endregion

}
