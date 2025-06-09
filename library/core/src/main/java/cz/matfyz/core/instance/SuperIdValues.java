package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
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
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Immutable.
 */
@JsonSerialize(using = SuperIdValues.Serializer.class)
@JsonDeserialize(using = SuperIdValues.Deserializer.class)
public class SuperIdValues implements Serializable, Comparable<SuperIdValues> {

    protected final Map<Signature, String> tuples;

    public boolean hasSignature(Signature signature) {
        return tuples.containsKey(signature);
    }

    public Set<Signature> signatures() {
        return tuples.keySet();
    }

    public String getValue(Signature signature) {
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

    public boolean containsId(SignatureId id) {
        for (var signature : id.signatures())
            if (!hasSignature(signature))
                return false;
        return true;
    }

    public SuperIdValues findId(SignatureId id) {
        var builder = new Builder();

        for (var signature : id.signatures()) {
            var value = this.tuples.get(signature);
            if (value == null)
                return null;
            builder.add(signature, value);
        }

        return builder.build();
    }

    public @Nullable SuperIdValues tryFindFirstId(ObjexIds ids) {
        for (final var id : ids.toSignatureIds())
            if (containsId(id))
                return findId(id);

        return null;
    }

    public record FindIdsResult(Set<SuperIdValues> foundIds, Set<SignatureId> notFoundIds) {}

    /**
     * Returns all ids that are contained there as a subset.
     * @param signatureIds The ids we want to find.
     * @return A set of found ids and also not found ids.
     */
    public FindIdsResult findAllIds(ObjexIds ids) {
        return findAllSignatureIds(ids.toSignatureIds());
    }

    public FindIdsResult findAllSignatureIds(Set<SignatureId> missingIds) {
        final var foundIds = new TreeSet<SuperIdValues>();
        final var notFoundIds = new TreeSet<SignatureId>();

        for (final SignatureId missingId : missingIds) {
            final var foundId = findId(missingId);

            if (foundId == null)
                notFoundIds.add(missingId);
            else
                foundIds.add(foundId);
        }

        return new FindIdsResult(foundIds, notFoundIds);
    }

    public static SuperIdValues fromEmptySignature(String value) {
        return new Builder().add(Signature.createEmpty(), value).build();
    }

    public static SuperIdValues empty() {
        return new SuperIdValues(new TreeMap<>());
    }

    private SuperIdValues(Map<Signature, String> map) {
        this.tuples = map;
    }

    public static class Builder {

        private Map<Signature, String> map = new TreeMap<>();

        public Builder add(Signature signature, String value) {
            map.put(signature, value);
            return this;
        }

        public Builder add(SuperIdValues idWithValues) {
            for (var tuple : idWithValues.tuples.entrySet())
                map.put(tuple.getKey(), tuple.getValue());

            return this;
        }

        public SuperIdValues build() {
            var output = new SuperIdValues(map);
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
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        boolean notFirst = false;
        for (var entry : tuples.entrySet()) {
            if (notFirst)
                builder.append(", ");
            else
                notFirst = true;

            builder.append("(").append(entry.getKey()).append(": \"").append(entry.getValue()).append("\")");
        }
        builder.append("}");

        return builder.toString();
    }

    public static class Serializer extends StdSerializer<SuperIdValues> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SuperIdValues> t) {
            super(t);
        }

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

        public Deserializer() {
            this(null);
        }

        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader signatureJsonReader = new ObjectMapper().readerFor(Signature.class);

        @Override public SuperIdValues deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);
            final Map<Signature, String> tuples = new TreeMap<>();

            final var iterator = node.elements();
            while (iterator.hasNext()) {
                final JsonNode object = iterator.next();
                final Signature signature = signatureJsonReader.readValue(object.get("signature"));
                final String value = object.get("value").asText();
                tuples.put(signature, value);
            }

            return new SuperIdValues(tuples);
        }

    }

}
