package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.SuperIdValues.IdComparator;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.UniqueSequentialGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Each object from instance category is modeled as a set of tuples ({@link DomainRow}).
 */
public class InstanceObjex implements Identified<InstanceObjex, Key> {

    public final SchemaObjex schema;
    private final InstanceCategory instance;

    /**
     * All rows in the domain of this object (even those without any id).
     * We need this because some rows don't have any id (or we didn't discover the ids yet).
     */
    private final Set<DomainRow> domain = new TreeSet<>();

    /**
     * All rows in the domain of this object (which have at least one id).
     * The first map is indexed by the different identifiers the object can have (they have to be signatureIds).
     * The second map is the actual rows, indexed by actual values of these identifiers.
     */
    private final Map<SignatureId, Map<SuperIdValues, DomainRow>> domainByIds = new TreeMap<>();

    Map<SuperIdValues, DomainRow> getRowsById(SignatureId id) {
        return domainByIds.computeIfAbsent(id, i -> new TreeMap<>(new IdComparator(id)));
    }

    public InstanceObjex(SchemaObjex schema, InstanceCategory instance, Collection<BaseSignature> dependentObjexes) {
        this.schema = schema;
        this.instance = instance;

        for (final var signature : dependentObjexes)
            assert !signature.isDual() : "Property signatures cannot be dual: " + signature;

        propertySignatures = new TreeSet<>(dependentObjexes);
    }

    private final SortedSet<BaseSignature> propertySignatures;

    /** Signatures of objexes whose values should be stored in the domain row (and which are not already part of the superId). */
    public Collection<BaseSignature> propertySignatures() {
        return propertySignatures;
    }

    public boolean isEmpty() {
        return domain.isEmpty();
    }

    /** Finds the row by any id that can be found in the given values. */
    public @Nullable DomainRow tryFindRow(SuperIdValues values) {
        for (final var id : schema.ids().signatureIds()) {
            if (!values.containsId(id))
                continue;

            final var row = getRowsById(id).get(values);
            if (row != null)
                return row;
        }

        return null;
    }

    /** The package access is just for serialization. */
    UniqueSequentialGenerator rowIdGenerator = UniqueSequentialGenerator.create();

    /**
     * Creates a new domain row with the given values.
     * This method doesn't merge the values, it just creates a new row with the given values. So make sure you check that the merging is not necessary before calling it.
     * If it is, call the `merge` methods instead.
     */
    public DomainRow createRow(SuperIdValues values) {
        final var row = new DomainRow(values, rowIdGenerator.next(), references.keySet());
        setRow(row);
        return row;
    }

    /**
     * Adds the row to the domain (by all of its ids) and to the domain by surrogate ids.
     */
    void setRow(DomainRow row) {
        final var ids = row.superId.findAllIds(schema.ids());
        for (final var id : ids)
            getRowsById(id).put(row.superId, row);

        domain.add(row);
    }

    /**
     * Iteratively merges the values to the row, adding all values discovered in the process. If multiple rows need to be merged, a new row is created.
     * Call this if you know the values contain some new information.
     */
    public DomainRow mergeValues(DomainRow row, SuperIdValues values) {
        final var merger = new InstanceMerger(instance);
        merger.addMergeJob(this, row, values);
        merger.processQueues();
        return merger.getTrackedRow();
    }

    /**
     * Propagates the references from the row to other rows. As a result of the propagation, a merging may happen, causing a new row to be created.
     * Call this whenever a new row is created (and is not merged already).
     */
    public DomainRow mergeReferences(DomainRow row) {
        final var merger = new InstanceMerger(instance);
        merger.addReferenceJob(this, row);
        merger.processQueues();
        return merger.getTrackedRow();
    }

    /** Removes rows from the domain. Doesn't remove them from the {@link #domainByIds} because they are expected to be overwriten there anyway. */
    void removeRows(Iterable<DomainRow> rows) {
        for (final DomainRow row : rows)
            domain.remove(row);
    }

    public Iterable<DomainRow> allRows() {
        return domain;
    }

    /** Groups references by the signature from the superId of this objex. */
    private final Map<Signature, Set<Reference>> references = new TreeMap<>();

    public void addReference(Signature signatureInThis, SchemaPath path, Signature signatureInOther) {
        final var referencesForSignature = references.computeIfAbsent(signatureInThis, x -> new TreeSet<>());
        referencesForSignature.add(new Reference(signatureInThis, path, signatureInOther));
    }

    public Set<Reference> getReferencesForSignature(Signature signatureInThis) {
        return references.get(signatureInThis);
    }

    public static record Reference(
        /** Part of superId of this objex. */
        Signature signatureInThis,
        /** Path from this objex to the referenced objex. */
        SchemaPath path,
        /** Part of superId of referenced objex. */
        Signature signatureInOther
    ) implements Comparable<Reference> {

        @Override public boolean equals(Object object) {
            if (this == object)
                return true;

            return object instanceof Reference reference
                && signatureInThis.equals(reference.signatureInThis)
                && path.equals(reference.path)
                && signatureInOther.equals(reference.signatureInOther);
        }

        @Override public int compareTo(Reference reference) {
            if (this == reference)
                return 0;

            final var x1 = signatureInThis.compareTo(reference.signatureInThis);
            if (x1 != 0)
                return x1;

            final var x2 = path.signature().compareTo(reference.path.signature());
            if (x2 != 0)
                return x2;

            return signatureInOther.compareTo(reference.signatureInOther);
        }

    }

    public void copyRowsFrom(InstanceObjex source) {
        // The output rows should be unique, so we have to keep them somewhere.
        final var rowsById = new TreeMap<Integer, DomainRow>();

        // TODO Use functions above to insert rows.
        // FIXME This is not correct (yet), because it doesn't use correct ids (the ids do have to change because the morphisms that form them have to change).

        // TODO How to copy signature ids?
        for (final var domainEntry : source.domainByIds.entrySet()) {
            final SignatureId id = domainEntry.getKey();
            final var sourceRows = domainEntry.getValue();
            final var targetRows = getRowsById(id);

            for (final DomainRow sourceRow : sourceRows.values()) {
                // TODO Pending references (probably should be empty, but we should check it).
                final DomainRow targetRow = rowsById.computeIfAbsent(sourceRow.surrogateId, x -> new DomainRow(sourceRow.superId, x, Set.of()));
                targetRows.put(targetRow.superId, targetRow);
            }
        }

        // Finally, copy (or just reuse) all rows to the domain.
        for (final var sourceRow : source.domain) {
            final DomainRow targetRow = rowsById.computeIfAbsent(sourceRow.surrogateId, x -> new DomainRow(sourceRow.superId, x, Set.of()));
            domain.add(targetRow);
        }
    }

    // Identification

    @Override public Key identifier() {
        return schema.key();
    }

    @Override public boolean equals(Object other) {
        return other instanceof InstanceObjex instanceObject && instanceObject.schema.equals(schema);
    }

    @Override public int hashCode() {
        return schema.hashCode();
    }

    // Debug

    @Override public String toString() {
        final var sb = new StringBuilder();

        sb.append("\tKey: ").append(schema.key());
        sb.append("\n");

        sb.append("\tValues:\n");
        for (final DomainRow row : allRows())
            sb.append("\t\t").append(row).append("\n");

        return sb.toString();
    }

}
