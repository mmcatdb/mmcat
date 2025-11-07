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

@JsonSerialize(using = ObjexIds.Serializer.class)
@JsonDeserialize(using = ObjexIds.Deserializer.class)
public class ObjexIds implements Serializable {

    /**
     * A set of signatures. If there is just one (nonempty), it's a simple identifier, otherwise it's a composite identifier.
     * The identifier are the values of the objexes to which the signatures point.
     * The objex has to be an entity.
     */
    private final SortedSet<SignatureId> signatureIds;

    public SignatureId first() {
        return signatureIds.first();
    }

    public Iterable<SignatureId> signatureIds() {
        return signatureIds.isEmpty() ? List.of(SignatureId.empty()) : signatureIds;
    }

    private ObjexIds(SortedSet<SignatureId> signatures) {
        this.signatureIds = signatures;
    }

    public static ObjexIds fromSet(Set<SignatureId> signatureIds) {
        return signatureIds.isEmpty() ? empty() : new ObjexIds(new TreeSet<>(signatureIds));
    }

    public static ObjexIds fromIds(SignatureId... signatureIds) {
        return fromSet(Set.of(signatureIds));
    }

    public static ObjexIds fromSignatures(Signature... signatures) {
        return fromSet(Set.of(SignatureId.fromSignatures(signatures)));
    }

    private static final ObjexIds emptyInstance = new ObjexIds(new TreeSet<>());

    /**
     * The objex is identified by a simple value (with an empty signature).
     * If the objex is an entity, the value has to be generated. Otherwise, it's just a property value.
     */
    public static ObjexIds empty() {
        return emptyInstance;
    }

    public boolean isEmpty() {
        return signatureIds.isEmpty();
    }

    public ObjexIds extend(SignatureId id) {
        final var newIds = new TreeSet<>(signatureIds);
        newIds.add(id);
        return signatureIds.size() == newIds.size() ? this : new ObjexIds(newIds);
    }

    public ObjexIds extend(Signature... signatures) {
        return signatures.length == 0 ? this : extend(SignatureId.fromSignatures(signatures));
    }

    public Set<Signature> collectAllSignatures() {
        if (signatureIds.isEmpty())
            return Set.of(Signature.empty());

        final var allSignatures = new TreeSet<Signature>();
        signatureIds.forEach(id -> id.signatures().forEach(allSignatures::add));

        return allSignatures;
    }

    @Override public String toString() {
        if (signatureIds.isEmpty())
            return "_EMPTY";

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
            generator.writeStartArray();
            for (final var id : ids.signatureIds)
                generator.writePOJO(id);
            generator.writeEndArray();
        }
    }

    public static class Deserializer extends StdDeserializer<ObjexIds> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public ObjexIds deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);
            final SignatureId[] signatureIds = codec.treeToValue(node, SignatureId[].class);
            return fromIds(signatureIds);
        }
    }

    // #endregion

}
