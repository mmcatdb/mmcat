package cz.matfyz.core.identifiers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
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
 * @author jachymb.bartik
 */
@JsonSerialize(using = ObjectIds.Serializer.class)
@JsonDeserialize(using = ObjectIds.Deserializer.class)
public class ObjectIds implements Serializable {

    public enum Type {
        /** A set of signatures. */
        Signatures,
        /** A simple string value. */
        Value,
        /** A simple string value that should be generated automatically. */
        Generated,
    }

    private final Type type;
    private final SortedSet<SignatureId> signatureIds;

    // TODO disable this method eventually and fix all other methods that rely on it.
    // The reason is that this whole object was introduced because we want to behave differently to the different types of ids - so there ain't be no function that unifies them back together.
    public SortedSet<SignatureId> toSignatureIds() {
        return isSignatures() ? new TreeSet<>(signatureIds) : new TreeSet<>(Set.of(SignatureId.createEmpty()));
    }

    public ObjectIds(Set<SignatureId> signatureIds) {
        this(new TreeSet<>(signatureIds));
    }

    public ObjectIds(Collection<SignatureId> signatureIds) {
        this(new TreeSet<>(signatureIds));
    }

    // There must be at least one signature
    public ObjectIds(SignatureId... signatureIds) {
        this(new TreeSet<>(List.of(signatureIds)));
    }

    public ObjectIds(Signature... signatures) {
        this(new TreeSet<>(List.of(new SignatureId(signatures))));
    }

    public static ObjectIds createValue() {
        return new ObjectIds(Type.Value);
    }

    public static ObjectIds createGenerated() {
        return new ObjectIds(Type.Generated);
    }

    private ObjectIds(SortedSet<SignatureId> signatures) {
        this.signatureIds = signatures;
        this.type = Type.Signatures;
    }

    private ObjectIds(Type type) {
        assert type != Type.Signatures;

        this.signatureIds = null;
        this.type = type;
    }

    public boolean isSignatures() {
        return type == Type.Signatures;
    }

    public boolean isValue() {
        return type == Type.Value;
    }

    public boolean isGenerated() {
        return type == Type.Generated;
    }

    public SignatureId generateDefaultSuperId() {
        if (type != Type.Signatures)
            return SignatureId.createEmpty();

        final var allSignatures = new TreeSet<Signature>();
        signatureIds.forEach(id -> allSignatures.addAll(id.signatures()));

        return new SignatureId(allSignatures);
    }

    @Override public String toString() {
        if (type == Type.Value)
            return "_VALUE";

        if (type == Type.Generated)
            return "_GENERATED";

        StringBuilder builder = new StringBuilder();

        builder.append("(");
        for (SignatureId signatureId : signatureIds.headSet(signatureIds.last()))
            builder.append(signatureId).append(", ");
        builder.append(signatureIds.last());
        builder.append(")");

        return builder.toString();
    }

    public static class Serializer extends StdSerializer<ObjectIds> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<ObjectIds> t) {
            super(t);
        }

        @Override public void serialize(ObjectIds ids, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("type", ids.type.name());

            if (ids.signatureIds != null) {
                generator.writeArrayFieldStart("signatureIds");
                for (final var id : ids.signatureIds)
                    generator.writePOJO(id);

                generator.writeEndArray();
            }

            generator.writeEndObject();
        }

    }

    public static class Deserializer extends StdDeserializer<ObjectIds> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader signatureIdsJsonReader = new ObjectMapper().readerFor(SignatureId[].class);

        @Override public ObjectIds deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Type type = Type.valueOf(node.get("type").asText());
            final SignatureId[] signatureIds = node.hasNonNull("signatureIds")
                ? signatureIdsJsonReader.readValue(node.get("signatureIds"))
                : null;

            return type == Type.Signatures ? new ObjectIds(signatureIds) : new ObjectIds(type);
        }

    }

}
