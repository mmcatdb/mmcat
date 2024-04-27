package cz.matfyz.core.identifiers;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
 * Id is a set of signatures (each corresponding to a base or a composite morphism).
 */
@JsonSerialize(using = SignatureId.Serializer.class)
@JsonDeserialize(using = SignatureId.Deserializer.class)
public class SignatureId implements Serializable, Comparable<SignatureId> {

    private final SortedSet<Signature> signatures;

    // TODO make immutable
    public SortedSet<Signature> signatures() {
        return signatures;
    }

    public SignatureId(Set<Signature> signatures) {
        this(new TreeSet<>(signatures));
    }

    // There must be at least one signature
    public SignatureId(Signature... signatures) {
        this(new TreeSet<>(List.of(signatures)));
    }

    public static SignatureId createEmpty() {
        return new SignatureId(Signature.createEmpty());
    }

    private SignatureId(SortedSet<Signature> signatures) {
        this.signatures = signatures;
    }

    public boolean hasSignature(Signature signature) {
        return this.signatures.contains(signature);
    }

    public boolean hasOnlyEmptySignature() {
        return this.signatures.size() == 1 && this.signatures.first().isEmpty();
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
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(signatures.stream().map(Object::toString).collect(Collectors.joining(", ")));
        builder.append(")");

        return builder.toString();
    }

    public static class Serializer extends StdSerializer<SignatureId> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SignatureId> t) {
            super(t);
        }

        @Override public void serialize(SignatureId signatureId, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (final var signature : signatureId.signatures)
                generator.writePOJO(signature);

            generator.writeEndArray();
        }

    }

    public static class Deserializer extends StdDeserializer<SignatureId> {

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader signaturesJsonReader = new ObjectMapper().readerFor(Signature[].class);

        @Override public SignatureId deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);

            final Signature[] signatures = signaturesJsonReader.readValue(node);

            return new SignatureId(signatures);
        }

    }

}
