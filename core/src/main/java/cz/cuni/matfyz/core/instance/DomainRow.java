package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.utils.IterableUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this class represents a tuple from the {@link InstanceObject}.
 * The tuple is made of pairs (signature, value) for each signature in the superid. This structure is implemented by a map.
 * Each value is unique among all the values associated with the same signature.
 * @author jachym.bartik
 */
@JsonSerialize(using = DomainRow.Serializer.class)
public class DomainRow implements Serializable, Comparable<DomainRow> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainRow.class);

    // The tuples that holds the value of this row.
    public final SuperIdWithValues superId;
    // All technical ids under which is this row known.
    public final Set<String> technicalIds;
    private final Set<Signature> pendingReferences;
    // Various ids that can be constructed from this row.

    public DomainRow(SuperIdWithValues superId, Set<String> technicalIds, Set<Signature> pendingReferences) {
        this.superId = superId;
        this.technicalIds = technicalIds;
        this.pendingReferences = pendingReferences;
    }

    public boolean hasSignature(Signature signature) {
        return superId.hasSignature(signature);
    }

    public Set<Signature> signatures() {
        return superId.signatures();
    }

    public String getValue(Signature signature) {
        return superId.getValue(signature);
    }

    private final Map<InstanceMorphism, Set<MappingRow>> mappingsFrom = new TreeMap<>();
    //public final Map<InstanceMorphism, Set<MappingRow>> mappingsTo = new TreeMap<>();

    /**
     * Warning: this is a low-level method that works as intended only for the base morphisms. For the composite ones, an empty set might be returned even if connections exist.
     */
    public Set<MappingRow> getMappingsFromForMorphism(InstanceMorphism morphism) {
        var mappings = mappingsFrom.get(morphism);
        return mappings != null ? mappings : new TreeSet<>();
    }

    public Set<Entry<InstanceMorphism, Set<MappingRow>>> getAllMappingsFrom() {
        return mappingsFrom.entrySet();
    }

    void addMappingFrom(InstanceMorphism morphism, MappingRow mapping) {
        var mappingsOfSameType = mappingsFrom.computeIfAbsent(morphism, x -> new TreeSet<>());
        mappingsOfSameType.add(mapping);
    }

    void removeMappingFrom(InstanceMorphism morphism, MappingRow mapping) {
        var mappingsOfSameType = mappingsFrom.get(morphism);
        mappingsOfSameType.remove(mapping);
    }

    public Set<DomainRow> traverseThrough(InstanceMorphism path) {
        var currentSet = new TreeSet<DomainRow>();
        currentSet.add(this);

        for (var baseMorphism : path.bases()) {
            var nextSet = new TreeSet<DomainRow>();
            for (var row : currentSet) {
                var codomainRows = row.getMappingsFromForMorphism(baseMorphism).stream().map(MappingRow::codomainRow).toList();
                nextSet.addAll(codomainRows);
            }
            currentSet = nextSet;
        }

        return currentSet;
    }

    record SignatureWithValue(Signature signature, String value) {}

    List<SignatureWithValue> getAndRemovePendingReferencePairs() {
        var pendingSignatures = pendingReferences.stream().filter(this::hasSignature).toList();
        pendingReferences.removeAll(pendingSignatures);
        return pendingSignatures.stream().map(signature -> new SignatureWithValue(signature, getValue(signature))).toList();
    }

    @Override
    public int compareTo(DomainRow row) {
        final var superIdComparison = superId.compareTo(row.superId);
        if (superIdComparison != 0)
            return superIdComparison;

        for (final var technicalId : technicalIds)
            if (row.technicalIds.contains(technicalId))
                return 0;

        return IterableUtils.compareTwoIterables(technicalIds, row.technicalIds);
    }
    
    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(superId.toString());
        if (!technicalIds.isEmpty()) {
            builder.append("[");
            var notFirst = false;
            for (var technicalId : technicalIds) {
                if (notFirst)
                    builder.append(", ");
                notFirst = true;
                builder.append(technicalId);
            }
            builder.append("]");
        }

        return builder.toString();
    }
    
    // TODO change equals and compareTo to do == first
    @Override
    public boolean equals(Object object) {
        return object instanceof DomainRow row && superId.equals(row.superId);
    }

    public static class Serializer extends StdSerializer<DomainRow> {

        public Serializer() {
            this(null);
        }

        public Serializer(Class<DomainRow> t) {
            super(t);
        }

        @Override
        public void serialize(DomainRow row, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writePOJOField("superId", row.superId);
            generator.writeFieldName("technicalIds");
            generator.writeArray(row.technicalIds.stream().toArray(String[]::new), 0, row.technicalIds.size());
            generator.writeEndObject();
        }

    }
    
}
