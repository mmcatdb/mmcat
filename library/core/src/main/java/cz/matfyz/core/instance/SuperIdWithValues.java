package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author jachymb.bartik
 */
@JsonSerialize(using = SuperIdWithValues.Serializer.class)
public class SuperIdWithValues implements Serializable, Comparable<SuperIdWithValues> {

    private final Map<Signature, String> tuples;

    public boolean hasSignature(Signature signature) {
        return tuples.containsKey(signature);
    }

    public Set<Signature> signatures() {
        return tuples.keySet();
    }

    public String getValue(Signature signature) {
        return tuples.get(signature);
    }

    private SignatureId cachedId;

    public SignatureId id() {
        if (cachedId == null)
            cachedId = new SignatureId(tuples.keySet());
        return cachedId;
        // Evolution extension
        //return new Id(map.keySet());
    }

    public Collection<String> values() {
        return tuples.values();
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

    public SuperIdWithValues findId(SignatureId id) {
        var builder = new Builder();

        for (var signature : id.signatures()) {
            var value = this.tuples.get(signature);
            if (value == null)
                return null;
            builder.add(signature, value);
        }

        return builder.build();
    }

    public SuperIdWithValues findFirstId(ObjectIds ids) {
        for (var id : ids.toSignatureIds())
            if (containsId(id))
                return findId(id);

        return null;
    }

    public record FindIdsResult(Set<SuperIdWithValues> foundIds, Set<SignatureId> notFoundIds) {}

    /**
     * Returns all ids that are contained there as a subset.
     * @param signatureIds The ids we want to find.
     * @return A set of found ids and also not found ids.
     */
    public FindIdsResult findAllIds(ObjectIds ids) {
        return findAllSignatureIds(ids.toSignatureIds());
    }

    public FindIdsResult findAllSignatureIds(Set<SignatureId> ids) {
        final var foundIds = new TreeSet<SuperIdWithValues>();
        final var notFoundIds = new TreeSet<SignatureId>();

        for (SignatureId id : ids) {
            var foundId = findId(id);

            if (foundId == null)
                notFoundIds.add(id);
            else
                foundIds.add(foundId);
        }

        return new FindIdsResult(foundIds, notFoundIds);
    }

    public static SuperIdWithValues merge(SuperIdWithValues... ids) {
        var builder = new Builder();
        for (var id : ids)
            builder.add(id);

        return builder.build();
    }

    public static SuperIdWithValues fromEmptySignature(String value) {
        return new Builder().add(Signature.createEmpty(), value).build();
    }

    public static SuperIdWithValues createEmpty() {
        return new Builder().build();
    }

    private SuperIdWithValues(Map<Signature, String> map) {
        this.tuples = map;
    }

    public static class Builder {

        private Map<Signature, String> map = new TreeMap<>();

        public Builder add(Signature signature, String value) {
            map.put(signature, value);
            return this;
        }

        public Builder add(SuperIdWithValues idWithValues) {
            for (var tuple : idWithValues.tuples.entrySet())
                map.put(tuple.getKey(), tuple.getValue());

            return this;
        }

        public SuperIdWithValues build() {
            var output = new SuperIdWithValues(map);
            map = new TreeMap<>();
            return output;
        }

    }

    @Override public boolean equals(Object object) {
        if (!(object instanceof SuperIdWithValues idWithValues))
            return false;

        return Objects.equals(this.tuples, idWithValues.tuples);
    }

    @Override public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.tuples);
        return hash;
    }

    @Override public int compareTo(SuperIdWithValues idWithValues) {
        int idCompareResult = id().compareTo(idWithValues.id());
        if (idCompareResult != 0)
            return idCompareResult;

        for (Signature signature : signatures()) {
            int signatureCompareResult = tuples.get(signature).compareTo(idWithValues.tuples.get(signature));
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

    public static class Serializer extends StdSerializer<SuperIdWithValues> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<SuperIdWithValues> t) {
            super(t);
        }

        @Override public void serialize(SuperIdWithValues superId, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartArray();
            for (final var entry : superId.tuples.entrySet()) {
                generator.writeStartObject();
                generator.writePOJOField("signature", entry.getKey());
                generator.writeStringField("value", entry.getValue());
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }

    }

}
