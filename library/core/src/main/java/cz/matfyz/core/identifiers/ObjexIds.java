package cz.matfyz.core.identifiers;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

@JsonSerialize(using = ObjexIds.Serializer.class)
@JsonDeserialize(using = ObjexIds.Deserializer.class)
public class ObjexIds implements Serializable {

    public enum Type {
        /**
         * A set of signatures. If there is just one, it's a simple identifier, otherwise it's a composite identifier.
         * The identifier are the values of the objexes to which the signatures point.
         * The objex has to be an entity.
         */
        Signatures,
        /**
         * A simple value which we don't know so it has to be generated.
         * The objex has to be an entity.
         */
        Generated,
        /**
         * A simple value.
         * The objex has to be a property.
         */
        Value,
    }

    private final Type type;
    private final @Nullable SortedSet<SignatureId> signatureIds;

    public SignatureId first() {
        return signatureIds.first();
    }

    public SortedSet<SignatureId> signatureIds() {
        return new TreeSet<>(signatureIds);
    }

    public Iterable<SignatureId> toSignatureIds() {
        return isSignatures() ? signatureIds : List.of(SignatureId.empty());
    }

    public ObjexIds(Set<SignatureId> signatureIds) {
        this(new TreeSet<>(signatureIds));
    }

    // There must be at least one signature
    public ObjexIds(SignatureId... signatureIds) {
        this(Set.of(signatureIds));
    }

    public ObjexIds(Signature... signatures) {
        this(Set.of(new SignatureId(signatures)));
    }

    private ObjexIds(SortedSet<SignatureId> signatures) {
        this.signatureIds = signatures;
        this.type = Type.Signatures;
    }

    private ObjexIds(Type type) {
        assert type != Type.Signatures;

        this.signatureIds = null;
        this.type = type;
    }

    public static ObjexIds createValue() {
        return new ObjexIds(Type.Value);
    }

    public static ObjexIds createGenerated() {
        return new ObjexIds(Type.Generated);
    }

    public ObjexIds extend(SignatureId id) {
        assert isSignatures();
        final var newIds = new TreeSet<>(signatureIds);
        newIds.add(id);
        return new ObjexIds(newIds);
    }

    /** @deprecated */
    public boolean isSignatures() {
        return type == Type.Signatures;
    }

    /** @deprecated */
    public boolean isGenerated() {
        return type == Type.Generated;
    }

    // public boolean isValue() {
    //     return type == Type.Value;
    // }

    public Set<Signature> collectAllSignatures() {
        if (type != Type.Signatures)
            return Set.of(Signature.empty());

        final var allSignatures = new TreeSet<Signature>();
        signatureIds.forEach(id -> allSignatures.addAll(id.signatures()));

        return allSignatures;
    }

    @Override public String toString() {
        if (type == Type.Value)
            return "_VALUE";

        if (type == Type.Generated)
            return "_GENERATED";

        final var sb = new StringBuilder();

        sb.append("(");
        for (SignatureId signatureId : signatureIds.headSet(signatureIds.last()))
            sb.append(signatureId).append(", ");
        sb.append(signatureIds.last());
        sb.append(")");

        return sb.toString();
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<ObjexIds> {
        public Serializer() { this(null); }
        public Serializer(Class<ObjexIds> t) { super(t); }

        @Override public void serialize(ObjexIds ids, JsonGenerator generator, SerializerProvider provider) throws IOException {
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

    public static class Deserializer extends StdDeserializer<ObjexIds> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public ObjexIds deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);

            final Type type = Type.valueOf(node.get("type").asText());
            final SignatureId[] signatureIds = node.hasNonNull("signatureIds")
                ? codec.treeToValue(node.get("signatureIds"), SignatureId[].class)
                : null;

            return type == Type.Signatures ? new ObjexIds(signatureIds) : new ObjexIds(type);
        }
    }

    // #endregion

}
