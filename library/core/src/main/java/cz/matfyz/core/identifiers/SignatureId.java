package cz.matfyz.core.identifiers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Id is a set of signatures (each corresponding to a base or a composite morphism).
 */
@JsonSerialize(using = SignatureId.Serializer.class)
@JsonDeserialize(using = SignatureId.Deserializer.class)
public class SignatureId implements Serializable, Comparable<SignatureId> {

    private final SortedSet<Signature> signatures;

    public int size() {
        return signatures.size();
    }

    public Iterable<Signature> signatures() {
        return signatures;
    }

    public Signature first() {
        return signatures.first();
    }

    private SignatureId(SortedSet<Signature> signatures) {
        this.signatures = signatures;
    }

    public static SignatureId fromSet(Set<Signature> signatures) {
        assert !signatures.isEmpty() : "Can't create SignatureId from empty set.";

        if (signatures.size() == 1 && signatures.iterator().next().isEmpty())
            return empty();

        for (final Signature signature : signatures)
            assert !signature.isEmpty() : "Can't create SignatureId with both empty signature and normal signatures.";

        return new SignatureId(new TreeSet<>(signatures));
    }

    public static SignatureId fromSignatures(Signature... signatures) {
        return fromSet(Set.of(signatures));
    }

    private static final SignatureId emptyInstance = new SignatureId(new TreeSet<>(Set.of(Signature.empty())));

    public static SignatureId empty() {
        return emptyInstance;
    }

    public boolean hasSignature(Signature signature) {
        return this.signatures.contains(signature);
    }

    /**
     * A signature empty is empty if it has exactly empty signature.
     * It always has to have at least one signature.
     */
    public boolean isEmpty() {
        return this == empty();
    }

    @Override public int compareTo(SignatureId id) {
        int sizeResult = signatures.size() - id.signatures.size();
        if (sizeResult != 0)
            return sizeResult;

        Iterator<Signature> iterator = id.signatures.iterator();

        for (Signature signature : signatures) {
            int signatureResult = signature.compareTo(iterator.next());
            if (signatureResult != 0)
                return signatureResult;
        }

        return 0;
    }

    @Override public boolean equals(Object object) {
        return object instanceof SignatureId id && compareTo(id) == 0;
    }

    @Override public String toString() {
        final var sb = new StringBuilder();

        sb.append("(");
        sb.append(signatures.stream().map(Object::toString).collect(Collectors.joining(", ")));
        sb.append(")");

        return sb.toString();
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<SignatureId> {
        public Serializer() { this(null); }
        public Serializer(Class<SignatureId> t) { super(t); }

        @Override public void serialize(SignatureId signatureId, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (final var signature : signatureId.signatures)
                generator.writePOJO(signature);

            generator.writeEndArray();
        }
    }

    public static class Deserializer extends StdDeserializer<SignatureId> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public SignatureId deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);
            final Signature[] signatures = codec.treeToValue(node, Signature[].class);
            return fromSignatures(signatures);
        }
    }

    // #endregion

}
