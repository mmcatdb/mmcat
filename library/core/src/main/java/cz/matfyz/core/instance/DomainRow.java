package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this class represents a tuple from the {@link InstanceObjex}.
 * The tuple is made of pairs (signature, value) for each signature in the superid. This structure is implemented by a map.
 * Each value is unique among all the values associated with the same signature.
 */
public class DomainRow implements Comparable<DomainRow> {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainRow.class);

    /** The tuples that holds the value of this row. Immutable. */
    public final SuperIdValues values;
    /** If defined, the object doesn't have enough values from the superId to form a valid identifier. */
    public final @Nullable Integer technicalId;
    /**
     * All signatures from the superId of this objex that point to values that are used in superIds of some other objexes.
     * Whenever a value for one of these signatures is found, the signature should be removed from this set and the value should be propagated to the referenced objexes.
     */
    public final Set<Signature> pendingReferences;

    public DomainRow(SuperIdValues values, @Nullable Integer technicalId, Set<Signature> pendingReferences) {
        this.values = values;
        this.technicalId = technicalId;
        this.pendingReferences = pendingReferences;
    }

    public boolean hasSignature(Signature signature) {
        return values.hasSignature(signature);
    }

    public Set<Signature> signatures() {
        return values.signatures();
    }

    public String getValue(Signature signature) {
        return values.getValue(signature);
    }

    public record MappingsFor(InstanceMorphism morphism, Set<MappingRow> mappings) {
        public MappingsFor(InstanceMorphism morphism) {
            this(morphism, new TreeSet<>());
        }
    }

    // These properties are managed by the morphisms, so they should not be cloned
    private final Map<Signature, MappingsFor> mappingsFrom = new TreeMap<>();
    private final Map<Signature, MappingsFor> mappingsTo = new TreeMap<>();

    public Set<MappingRow> getMappingsForEdge(SchemaEdge edge) {
        final var mappingsFor = (edge.direction() ? mappingsFrom : mappingsTo).get(edge.morphism().signature());
        return mappingsFor != null ? mappingsFor.mappings : Set.of();
    }

    public List<DomainRow> getCodomainForEdge(SchemaEdge edge) {
        return getMappingsForEdge(edge).stream().map(mappingRow -> edge.direction() ? mappingRow.codomainRow() : mappingRow.domainRow()).toList();
    }

    public Collection<MappingsFor> getAllMappingsFrom() {
        return mappingsFrom.values();
    }

    void addMappingFrom(InstanceMorphism morphism, MappingRow mapping) {
        final var mappingsOfSameType = mappingsFrom.computeIfAbsent(morphism.schema.signature(), x -> new MappingsFor(morphism));
        mappingsOfSameType.mappings.add(mapping);
    }

    void removeMappingFrom(InstanceMorphism morphism, MappingRow mapping) {
        final var mappingsOfSameType = mappingsFrom.get(morphism.schema.signature());
        mappingsOfSameType.mappings.remove(mapping);
    }

    void addMappingTo(InstanceMorphism morphism, MappingRow mapping) {
        final var mappingsOfSameType = mappingsTo.computeIfAbsent(morphism.schema.signature(), x -> new MappingsFor(morphism));
        mappingsOfSameType.mappings.add(mapping);
    }

    void removeMappingTo(InstanceMorphism morphism, MappingRow mapping) {
        final var mappingsOfSameType = mappingsTo.get(morphism.schema.signature());
        mappingsOfSameType.mappings.remove(mapping);
    }

    public Set<DomainRow> traverseThrough(SchemaPath path) {
        var currentSet = new TreeSet<DomainRow>();
        currentSet.add(this);

        for (final var edge : path.edges()) {
            final var nextSet = new TreeSet<DomainRow>();
            for (final var row : currentSet)
                nextSet.addAll(row.getCodomainForEdge(edge));

            currentSet = nextSet;
        }

        return currentSet;
    }

    record SignatureWithValue(
        /** A signature from this row's superId. */
        Signature signature,
        /** Value corresponding to the signature. */
        String value
    ) {}

    List<SignatureWithValue> getAndRemovePendingReferencePairs() {
        final var pendingSignatures = pendingReferences.stream().filter(this::hasSignature).toList();
        pendingReferences.removeAll(pendingSignatures);

        return pendingSignatures.stream().map(signature -> new SignatureWithValue(signature, getValue(signature))).toList();
    }

    @Override public int compareTo(DomainRow other) {
        final var valuesComparison = values.compareTo(other.values);
        if (valuesComparison != 0)
            return valuesComparison;

        if (technicalId == null)
            return technicalId == null ? 0 : 1;

        return other.technicalId == null ? -1 : technicalId.compareTo(other.technicalId);
    }

    @Override public String toString() {
        final var builder = new StringBuilder();
        builder.append(values.toString());
        if (technicalId != null)
            builder.append("#").append(technicalId);

        return builder.toString();
    }

    // TODO change equals and compareTo to do == first
    @Override public boolean equals(Object object) {
        return object instanceof DomainRow row && values.equals(row.values);
    }

}
