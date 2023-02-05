package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.Identified;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

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
 * @author pavel.koupil, jachym.bartik
 */
@JsonSerialize(using = SchemaMorphism.Serializer.class)
@JsonDeserialize(using = SchemaMorphism.Deserializer.class)
public class SchemaMorphism implements Serializable, Morphism, Identified<Signature> {
    
    private Signature signature;
    private SchemaObject dom;
    private SchemaObject cod;
    private Min min;
    private Max max;

    public String iri;
    public String pimIri;
    private Set<Tag> tags;

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public static Min combineMin(Min min1, Min min2) {
        return (min1 == Min.ONE && min2 == Min.ONE) ? Min.ONE : Min.ZERO;
    }

    public static Max combineMax(Max max1, Max max2) {
        return (max1 == Max.ONE && max2 == Max.ONE) ? Max.ONE : Max.STAR;
    }

    private SchemaCategory category;

    /*
    public static SchemaMorphism dual(SchemaMorphism morphism) {
        return SchemaMorphism.dual(morphism, 1, 1);
    }
    */

    /*
    public SchemaMorphism createDual(Min min, Max max) {
        SchemaMorphism result = new SchemaMorphism(signature.dual(), cod, dom, min, max);
        return result;
    }
    */

    //private SchemaMorphism(Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max)
    private SchemaMorphism(SchemaObject dom, SchemaObject cod) {
        //this.signature = signature;
        this.dom = dom;
        this.cod = cod;
        //this.min = min;
        //this.max = max;
    }

    public void setCategory(SchemaCategory category) {
        this.category = category;
    }

    @Override
    public SchemaObject dom() {
        return dom;
    }

    @Override
    public SchemaObject cod() {
        return cod;
    }

    @Override
    public Min min() {
        return min;
    }

    @Override
    public Max max() {
        return max;
    }

    public boolean isArray() {
        return max == Max.STAR;
    }

    public boolean isBase() {
        return signature.isBase();
    }

    @Override
    public SchemaMorphism dual() {
        return category.dual(signature);
    }

    @Override
    public Signature signature() {
        return signature;
    }

    @Override
    public Signature identifier() {
        return signature;
    }

    public static class Builder {

        public SchemaMorphism fromArguments(Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max) {
            var morphism = new SchemaMorphism(dom, cod);
            morphism.signature = signature;
            morphism.min = min;
            morphism.max = max;
            return morphism;
        }

        public SchemaMorphism fromDual(SchemaMorphism dualMorphism, Min min, Max max) {
            var dom = dualMorphism.cod;
            var cod = dualMorphism.dom;
            return fromArguments(dualMorphism.signature.dual(), dom, cod, min, max);
        }

    }

    public static class Serializer extends StdSerializer<SchemaMorphism> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SchemaMorphism> t) {
            super(t);
        }

        @Override
        public void serialize(SchemaMorphism morphism, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("signature", morphism.signature);
            generator.writePOJOField("dom", morphism.dom.identifier());
            generator.writePOJOField("cod", morphism.cod.identifier());
            generator.writeStringField("min", morphism.min.name());
            generator.writeStringField("max", morphism.max.name());
            generator.writeStringField("iri", morphism.iri);
            generator.writeStringField("pimIri", morphism.pimIri);
            generator.writeArrayFieldStart("tags");
            for (final var tag : morphism.tags)
                generator.writeString(tag.name());
            generator.writeEndArray();
            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<SchemaMorphism> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static ObjectReader keyJSONReader = new ObjectMapper().readerFor(Key.class);
        private static ObjectReader signatureJSONReader = new ObjectMapper().readerFor(Signature.class);
    
        @Override
        public SchemaMorphism deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Key domKey = keyJSONReader.readValue(node.get("dom"));
            final Key codKey = keyJSONReader.readValue(node.get("cod"));

            final SchemaCategory category = (SchemaCategory) context.getAttribute("category");

            final var morphism = new SchemaMorphism(category.getObject(domKey), category.getObject(codKey));

            morphism.signature = signatureJSONReader.readValue(node.get("signature"));
            morphism.min = Min.valueOf(node.get("min").asText());
            morphism.max = Max.valueOf(node.get("max").asText());
            morphism.iri = node.hasNonNull("iri") ? node.get("iri").asText() : "";
            morphism.pimIri = node.hasNonNull("pimIri") ? node.get("pimIri").asText() : "";

            morphism.tags = new TreeSet<>();
            if (node.hasNonNull("tags")) {
                final JsonNode tagsArray = node.get("tags");
                for (final var tagNode : tagsArray) {
                    morphism.tags.add(Tag.valueOf(tagNode.asText()));
                }
            }
    
            return morphism;
        }

    }

}
