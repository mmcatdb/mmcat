package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

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
    public final SuperIdValues superId;
    /** If defined, the object doesn't have enough values from the superId to form a valid identifier. */
    public final @Nullable Integer technicalId;

    // FIXME consider this when merging
    private final Map<BaseSignature, String> propertyValues = new TreeMap<>();

    /**
     * All signatures from the superId of this objex that point to values that are used in superIds of some other objexes.
     * Whenever a value for one of these signatures is found, the signature should be removed from this set and the value should be propagated to the referenced objexes.
     */
    public final Set<Signature> pendingReferences;

    public DomainRow(SuperIdValues superId, @Nullable Integer technicalId, Set<Signature> pendingReferences) {
        this.superId = superId;
        this.technicalId = technicalId;
        this.pendingReferences = pendingReferences;
    }

    /**
     * A scalar value is either a property value or a value from the superId.
     */
    public @Nullable String tryGetScalarValue(Signature signature) {
        final var superIdValue = superId.getValue(signature);
        if (superIdValue != null || !(signature instanceof final BaseSignature base))
            return superIdValue;

        return propertyValues.get(base);
    }

    /**
     * If the value is not here, traverses other rows and tries to find the value there.
     */
    public @Nullable String tryFindScalarValue(Signature signature) {
        final var superIdValue = superId.getValue(signature);
        if (superIdValue != null)
            return superIdValue;

        // A base signature can only be in propertyValues (if it's not in the superId).
        if (signature instanceof final BaseSignature base)
            return propertyValues.get(base);

        final var bases = signature.toBases();
        DomainRow current = this;
        for (int i = 0; i < bases.size() - 1; i++) {
            final var base = bases.get(i);
            // A scalar value can only be in a "from" mapping.
            final var mapping = current.mappingsFrom.get(base);
            if (mapping == null)
                return null;

            current = mapping.cod();
        }

        // If the value is somewhere, it should also be in the last base.
        // This might not be true during the creation of the rows, but let's just don't use this function in that case.
        final var lastBase = bases.get(bases.size() - 1);
        return current.tryGetScalarValue(lastBase);
    }

    /**
     * If the values are not here, traverses other rows and tries to find the values there.
     */
    public Collection<String> findArrayValues(Signature signature) {
        // Similarly to the function above, we traverse up to the last base.
        final var rows = traverseThrough(signature.cutLast());
        final var lastBase = signature.getLast();

        final Set<String> output = new TreeSet<>();

        // The last base can't be dual - we are retrieving a property value from some entity.
        assert !lastBase.isDual() : "The last base of a signature used to retrieve array values cannot be dual: " + signature;

        for (final var row : rows) {
            final var value = row.tryGetScalarValue(lastBase);
            if (value != null)
                output.add(value);
        }

        return output;
    }

    public void addPropertyValue(BaseSignature signature, String value) {
        propertyValues.put(signature, value);
    }

    // These properties are managed by the morphisms, so they shouldn't be cloned.

    /**
     * All mappings starting in this row by the signature of the corresponding morphism. There can be at most one such morphism for each signature.
     * I.e., the signature is absolute.
     */
    private final Map<BaseSignature, MappingRow> mappingsFrom = new TreeMap<>();
    /**
     * All mappings ending in this row by the signature of the corresponding morphism. There can be multiple such morphisms for each signature.
     * I.e., the signature is absolute.
     */
    private final Map<BaseSignature, Set<MappingRow>> mappingsTo = new TreeMap<>();

    public Set<MappingRow> getMappingsForEdge(SchemaEdge edge) {
        final var signature = edge.morphism().signature();

        if (edge.direction()) {
            final var mappingFrom = mappingsFrom.get(signature);
            return mappingFrom == null ? Set.of() : Set.of(mappingFrom);
        }

        return mappingsTo.getOrDefault(signature, Set.of());
    }

    public boolean hasMappingToOther(DomainRow other, SchemaEdge edgeToOther) {
        final var signature = edgeToOther.morphism().signature();

        if (edgeToOther.direction()) {
            final var mappingFrom = mappingsFrom.get(signature);
            if (mappingFrom == null)
                return false;

            return mappingFrom.cod() == other;
        }

        final var mappings = mappingsTo.get(signature);
        if (mappings == null)
            return false;

        for (final var mapping : mappings)
            if (mapping.dom() == other)
                return true;

        return false;
    }

    public @Nullable MappingRow getMappingFrom(BaseSignature signature) {
        return mappingsFrom.get(signature);
    }

    public Collection<Entry<BaseSignature, MappingRow>> getAllMappingsFrom() {
        return mappingsFrom.entrySet();
    }

    public Collection<Entry<BaseSignature, Set<MappingRow>>> getAllMappingsTo() {
        return mappingsTo.entrySet();
    }

    void setMappingFrom(InstanceMorphism morphism, MappingRow mapping) {
        mappingsFrom.put(morphism.schema.signature(), mapping);
    }

    void unsetMappingFrom(InstanceMorphism morphism) {
        mappingsFrom.remove(morphism.schema.signature());
    }

    void addMappingTo(InstanceMorphism morphism, MappingRow mapping) {
        final var mappingsOfSameType = mappingsTo.computeIfAbsent(morphism.schema.signature(), x -> new TreeSet<>());
        mappingsOfSameType.add(mapping);
    }

    void removeMappingTo(InstanceMorphism morphism, MappingRow mapping) {
        final var mappingsOfSameType = mappingsTo.get(morphism.schema.signature());
        mappingsOfSameType.remove(mapping);
    }

    public Set<DomainRow> traverseThrough(SchemaPath path) {
        Set<DomainRow> current = Set.of(this);

        for (final var edge : path.edges())
            current = collectCodomainRows(current, edge.direction(), edge.absoluteSignature());

        return current;
    }

    public Set<DomainRow> traverseThrough(Signature signature) {
        Set<DomainRow> current = Set.of(this);

        for (final var base : signature.toBases())
            current = collectCodomainRows(current, !base.isDual(), base.toAbsolute());

        return current;
    }

    private Set<DomainRow> collectCodomainRows(Collection<DomainRow> prev, boolean direction, BaseSignature absoluteBase) {
        final var next = new TreeSet<DomainRow>();

        if (direction) {
            for (final var row : prev) {
                final var mappingFrom = row.mappingsFrom.get(absoluteBase);
                if (mappingFrom != null)
                    next.add(mappingFrom.cod());
            }
        }
        else {
            for (final var row : prev) {
                final var mappings = row.mappingsTo.get(absoluteBase);
                if (mappings != null) {
                    for (final var mappingRow : mappings)
                        next.add(mappingRow.dom());
                }
            }
        }

        return next;
    }

    record SignatureWithValue(
        /** A signature from this row's superId. */
        Signature signature,
        /** Value corresponding to the signature. */
        String value
    ) {}

    List<SignatureWithValue> getAndRemovePendingReferencePairs() {
        final var pendingSignatures = pendingReferences.stream().filter(s -> superId.hasSignature(s)).toList();
        pendingReferences.removeAll(pendingSignatures);

        return pendingSignatures.stream().map(signature -> new SignatureWithValue(signature, tryGetScalarValue(signature))).toList();
    }

    @Override public int compareTo(DomainRow other) {
        if (this == other)
            return 0;

        final var superIdComparison = superId.compareTo(other.superId);
        if (superIdComparison != 0)
            return superIdComparison;

        if (technicalId == null)
            return technicalId == null ? 0 : 1;

        return other.technicalId == null ? -1 : technicalId.compareTo(other.technicalId);
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
        sb.append(superId.toStringWithoutGeneratedIds(idValue));
        if (technicalId != null)
            sb.append("#").append(technicalId);

        sb.append(": (");
        final var SEPARATOR = ", ";

        for (final var entry : propertyValues.entrySet()) {
            sb
                .append(entry.getKey()).append(": \"").append(entry.getValue()).append("\"")
                .append(SEPARATOR);
        }

        if (!propertyValues.isEmpty())
            sb.setLength(sb.length() - SEPARATOR.length());

        sb.append(")");

        return sb.toString();
    }

    // There is no notion of equality for DomainRow, so we don't override equals.
    // In practice, they are identified by the superId values and technicalId. However, they should be unique by these values in the context of one instance objex.
    // So, if two rows have the same values, they have to be referentially equal.

}
