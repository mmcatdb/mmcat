package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
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

/**
 * Immutable.
 */
@JsonSerialize(using = SuperIdValues.Serializer.class)
@JsonDeserialize(using = SuperIdValues.Deserializer.class)
public class SuperIdValues implements Serializable, Comparable<SuperIdValues> {

    protected final Map<Signature, String> tuples;

    private SuperIdValues(Map<Signature, String> map) {
        this.tuples = map;
    }

    public static SuperIdValues fromEmptySignature(String value) {
        return new Builder().add(Signature.empty(), value).build();
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

    private @Nullable SignatureId cachedId;

    public SignatureId id() {
        if (cachedId == null)
            cachedId = new SignatureId(tuples.keySet());
        return cachedId;
    }

    public int size() {
        return tuples.size();
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    public @Nullable SuperIdValues tryFindFirstId(ObjexIds ids) {
        if (!ids.isSignatures())
            return tryFindId(SignatureId.empty());

        for (final var id : ids.signatureIds()) {
            final var found = tryFindId(id);
            if (found != null)
                return found;
        }

        return null;
    }

    private @Nullable SuperIdValues tryFindId(SignatureId id) {
        final var output = new TreeMap<Signature, String>();

        for (final var signature : id.signatures()) {
            final var value = this.tuples.get(signature);
            if (value == null)
                return null;

            output.put(signature, value);
        }

        return new SuperIdValues(output);
    }

    public record FindIdsResult(List<SuperIdValues> foundIds, Set<SignatureId> notFoundIds) {}

    /**
     * Returns all ids that are contained there as a subset.
     * @param signatureIds The ids we want to find.
     * @return A set of found ids and also not found ids.
     */
    public FindIdsResult findAllIds(ObjexIds ids) {
        return findAllSignatureIds(ids.toSignatureIds());
    }

    public FindIdsResult findAllSignatureIds(Iterable<SignatureId> missingIds) {
        final var foundIds = new ArrayList<SuperIdValues>();
        final var notFoundIds = new TreeSet<SignatureId>();

        for (final SignatureId missingId : missingIds) {
            final var found = tryFindId(missingId);
            if (found != null)
                foundIds.add(found);
            else
                notFoundIds.add(missingId);
        }

        return new FindIdsResult(foundIds, notFoundIds);
    }

    public static class Builder {

        private Map<Signature, String> map = new TreeMap<>();

        public Builder add(Signature signature, String value) {
            map.put(signature, value);
            return this;
        }

        public Builder add(SuperIdValues idWithValues) {
            for (final var tuple : idWithValues.tuples.entrySet())
                map.put(tuple.getKey(), tuple.getValue());

            return this;
        }

        public SuperIdValues build() {
            final var output = new SuperIdValues(map);
            map = new TreeMap<>();
            return output;
        }

    }

    public static class Mutable extends SuperIdValues {

        public Mutable(@Nullable SuperIdValues input) {
            super(input == null ? new TreeMap<>() : new TreeMap<>(input.tuples));
        }

        public Mutable add(Signature signature, String value) {
            tuples.put(signature, value);
            return this;
        }

        public Mutable add(SuperIdValues idWithValues) {
            for (var tuple : idWithValues.tuples.entrySet())
                tuples.put(tuple.getKey(), tuple.getValue());

            return this;
        }

        public SuperIdValues build() {
            return new SuperIdValues(new TreeMap<>(tuples));
        }

    }

    @Override public boolean equals(Object object) {
        if (!(object instanceof SuperIdValues idWithValues))
            return false;

        return Objects.equals(this.tuples, idWithValues.tuples);
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.tuples);
        return hash;
    }

    @Override public int compareTo(SuperIdValues idWithValues) {
        final int idCompareResult = id().compareTo(idWithValues.id());
        if (idCompareResult != 0)
            return idCompareResult;

        for (final Signature signature : signatures()) {
            final int signatureCompareResult = tuples.get(signature).compareTo(idWithValues.tuples.get(signature));
            if (signatureCompareResult != 0)
                return signatureCompareResult;
        }

        return 0;
    }

    @Override public String toString() {
        return toStringWithoutGeneratedIds(null);
    }

    /**
     * Prints the row but replaces the generated id with the provided <code>idValue</code> (if there is such id and the value isn't null).
     * Useful for tests.
     */
    public String toStringWithoutGeneratedIds(@Nullable String idValue) {
        final var sb = new StringBuilder();

        sb.append("{");
        final var SEPARATOR = ", ";
        for (final var entry : tuples.entrySet()) {
            final var signature = entry.getKey();
            final var value = (signature.isEmpty() && idValue != null) ? idValue : entry.getValue();
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
