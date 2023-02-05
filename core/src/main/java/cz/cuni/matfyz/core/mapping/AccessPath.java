package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.FromJSONSwitchBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.util.Set;

/**
 * Common ancestor for the access path tree. It can be a {@link ComplexProperty} or a {@link SimpleProperty}.
 * Each node is a tuple (name, context, value).
 * @author pavel.koupil, jachym.bartik
 */
//@JsonSerialize(using = AccessPath.Serializer.class)
public abstract class AccessPath implements JSONConvertible {

    protected final Name name;
    
    public Name name() {
        return name;
    }
    
    protected AccessPath(Name name) {
        this.name = name;
    }
    
    protected abstract boolean hasSignature(Signature signature);
    
    @Override
    public boolean equals(Object object) {
        return object instanceof AccessPath path && name.equals(path.name);
    }
    
    public abstract Signature signature();

    public static class Builder extends FromJSONSwitchBuilderBase<AccessPath> {

        @Override
        protected Set<FromJSONBuilderBase<? extends AccessPath>> getChildConverters() {
            return Set.of(
                new ComplexProperty.Builder(),
                new SimpleProperty.Builder()
            );
        }

    }
/*
    public static class Serializer extends StdSerializer<Key> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<Key> t) {
            super(t);
        }

        @Override
        public void serialize(Key key, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("value", key.value);
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<Id> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }
    
        @Override
        public Id deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode node = parser.getCodec().readTree(parser);
    
            return new Id(node.asText());
        }

    }
*/
}
