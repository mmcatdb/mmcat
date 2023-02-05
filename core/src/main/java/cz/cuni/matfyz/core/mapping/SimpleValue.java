package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple value is a signature of morphism ?(which maps the parent property to this value)?
 * @author jachymb.bartik
 */
//@JsonSerialize(using = SimpleValue.Serializer.class)
public class SimpleValue implements JSONConvertible {
    
    private final Signature signature;
    
    public Signature signature() {
        return signature;
    }
    
    public SimpleValue(Signature signature) {
        this.signature = signature;
    }
    
    private static final SimpleValue empty = new SimpleValue(Signature.createEmpty());
    
    public static SimpleValue createEmpty() {
        return empty;
    }

    public boolean isEmpty() {
        return this.signature.isEmpty();
    }
    
    @Override
    public String toString() {
        return signature.toString();
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SimpleValue> {

        @Override
        protected JSONObject innerToJSON(SimpleValue object) throws JSONException {
            var output = new JSONObject();
    
            output.put("signature", object.signature.toJSON());
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<SimpleValue> {
    
        @Override
        protected SimpleValue innerFromJSON(JSONObject jsonObject) throws JSONException {
            var signature = new Signature.Builder().fromJSON(jsonObject.getJSONArray("signature"));
            
            return new SimpleValue(signature);
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
