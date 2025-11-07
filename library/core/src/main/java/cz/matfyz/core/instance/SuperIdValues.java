package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

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
 * Immutable.
 */
@JsonSerialize(using = SuperIdValues.Serializer.class)
@JsonDeserialize(using = SuperIdValues.Deserializer.class)
public class SuperIdValues implements Serializable {

    /** Actually immutable (except for a very specific use case in the {@link Mutator} class). */
    protected Map<Signature, String> tuples;

    private SuperIdValues(Map<Signature, String> map) {
        this.tuples = map;
    }

    public static SuperIdValues fromEmptySignature(String value) {
        return new Mutator().add(Signature.empty(), value).build();
    }

    public static SuperIdValues empty() {
        return new SuperIdValues(new TreeMap<>());
    }

    public boolean hasSignature(Signature signature) {
        return tuples.containsKey(signature);
    }

    public Set<Signature> signatures() {
        return tuples.keySet();
    }

    public @Nullable String getValue(Signature signature) {
        return tuples.get(signature);
    }

    public int size() {
        return tuples.size();
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    public boolean containsSomeIds(ObjexIds ids) {
        for (final var id : ids.signatureIds()) {
            final var found = containsId(id);
            if (found)
                return true;
        }

        return false;
    }

    public boolean containsId(SignatureId id) {
        for (final var signature : id.signatures()) {
            final var containsSignature = tuples.containsKey(signature);
            if (!containsSignature)
                return false;
        }

        return true;
    }

    /**
     * Returns all ids that are contained here as a subset.
     */
    public List<SignatureId> findAllIds(ObjexIds ids) {
        return findAllIds(ids.signatureIds());
    }

    /**
     * Returns all ids that are contained here as a subset.
     * @param signatureIds The ids we want to find.
     */
    public List<SignatureId> findAllIds(Iterable<SignatureId> ids) {
        final var output = new ArrayList<SignatureId>();

        for (final SignatureId id : ids) {
            if (containsId(id))
                output.add(id);
        }

        return output;
    }

    public static record IdComparator(
        SignatureId id
    ) implements Comparator<SuperIdValues> {

        @Override public int compare(SuperIdValues a, SuperIdValues b) {
            for (final Signature signature : id.signatures()) {
                final int result = a.tuples.get(signature).compareTo(b.tuples.get(signature));
                if (result != 0)
                    return result;
            }

            return 0;
        }

    }

    public static class Mutator extends SuperIdValues {

        public Mutator() {
            super(new TreeMap<>());
        }

        public Mutator(@Nullable SuperIdValues input) {
            super(input == null ? new TreeMap<>() : new TreeMap<>(input.tuples));
        }

        public Mutator add(Signature signature, String value) {
            tuples.put(signature, value);
            return this;
        }

        public Mutator add(SuperIdValues other) {
            tuples.putAll(other.tuples);
            return this;
        }

        public SuperIdValues build() {
            final var output = new SuperIdValues(tuples);
            // Prevent further modifications. Not the most elegant way, but it works.
            tuples = null;
            return output;
        }

    }

    @Override public boolean equals(Object object) {
        if (!(object instanceof SuperIdValues other))
            return false;

        return Objects.equals(tuples, other.tuples);
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(tuples);
        return hash;
    }

    // This class is not comparable by itself. Use `IdComparator` instead.

    @Override public String toString() {
        return toStringForTests(false);
    }

    /**
     * Prints the row but replaces the generated id with a fixed string value (if there is such id and the value isn't null).
     * Useful for tests.
     */
    public String toStringForTests(boolean isTest) {
        final var sb = new StringBuilder();

        sb.append("{");
        final var SEPARATOR = ", ";
        for (final var entry : tuples.entrySet()) {
            final var signature = entry.getKey();
            final var value = (signature.isEmpty() && isTest) ? "<generated>" : entry.getValue();
            sb
                .append(signature).append(": \"").append(value).append("\"")
                .append(SEPARATOR);
        }
        if (!tuples.isEmpty())
            sb.setLength(sb.length() - SEPARATOR.length());

        sb.append("}");

        return sb.toString();
    }

    // #region Serialization

    public static class Serializer extends StdSerializer<SuperIdValues> {
        public Serializer() { this(null); }
        public Serializer(Class<SuperIdValues> t) { super(t); }

        @Override public void serialize(SuperIdValues values, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (final var entry : values.tuples.entrySet()) {
                generator.writeStartObject();
                generator.writePOJOField("signature", entry.getKey());
                generator.writeStringField("value", entry.getValue());
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }
    }

    public static class Deserializer extends StdDeserializer<SuperIdValues> {
        public Deserializer() { this(null); }
        public Deserializer(Class<?> vc) { super(vc); }

        @Override public SuperIdValues deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final var codec = parser.getCodec();
            final JsonNode node = codec.readTree(parser);
            final Map<Signature, String> tuples = new TreeMap<>();

            final var iterator = node.elements();
            while (iterator.hasNext()) {
                final JsonNode object = iterator.next();
                final Signature signature = codec.treeToValue(object.get("signature"), Signature.class);
                final String value = object.get("value").asText();
                tuples.put(signature, value);
            }

            return new SuperIdValues(tuples);
        }
    }

    // #endregion

}
