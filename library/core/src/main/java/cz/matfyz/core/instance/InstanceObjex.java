package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.UniqueSequentialGenerator;

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
     * All rows in the domain of this object.
     * The first map is indexed by the different identifiers the object can have (they have to be signatureIds).
     * The second map is the actual rows, indexed by actual values of these identifiers.
     */
    final Map<SignatureId, Map<SuperIdValues, DomainRow>> domain = new TreeMap<>();
    /**
     * A technical id is a unique identifier that is used when the object does not have any other identifiers (or we don't know their values).
     */
    private final Map<Integer, DomainRow> domainByTechnicalIds = new TreeMap<>();

    public InstanceObjex(SchemaObjex schema, InstanceCategory instance) {
        this.schema = schema;
        this.instance = instance;
        this.technicalIdGenerator = UniqueSequentialGenerator.create();
    }

    public boolean isEmpty() {
        return domain.isEmpty() && domainByTechnicalIds.isEmpty();
    }

    /** Finds the row by any id that can be found in the given values. */
    public @Nullable DomainRow tryFindRow(SuperIdValues values) {
        for (final var id : values.findAllIds(schema.ids()).foundIds()) {
            final var row = tryFindRowById(id);
            if (row != null)
                return row;
        }

        return null;
    }

    /** Finds the row by the given id values. The given id valus has to be an id. */
    @Nullable DomainRow tryFindRowById(SuperIdValues idValues) {
        final Map<SuperIdValues, DomainRow> rowsWithSameTypeId = domain.get(idValues.id());
        return rowsWithSameTypeId == null ? null : rowsWithSameTypeId.get(idValues);
    }

    // The following `create` methods don't merge the values, they just create a new row with the given values.
    // So make sure you check that the merging is not necessary before calling them.
    // If it is, call the `merge` methods instead.

    /**
     * Creates a new domain row with the given values.
     * If the values do not contain any id, a technical id is generated and used.
     */
    public DomainRow createRow(SuperIdValues values) {
        final @Nullable Integer technicalId = values.tryFindFirstId(schema.ids()) == null
            ? generateTechnicalId()
            : null;
        return createRowInner(values, technicalId);
    }

    /**
     * Doesn't check whether the techical id is necessary or not!
     * Make sure the values contain at least one id.
     */
    public DomainRow createRowWithValueId(SuperIdValues values) {
        return createRowInner(values, null);
    }

    /**
     * Creates a new domain row with the given values and a technical id.
     * Make sure the values don't contain any id.
     */
    public DomainRow createRowWithTechnicalId(SuperIdValues values) {
        return createRowInner(values, generateTechnicalId());
    }

    /**
     * Doesn't check whether the techical id is necessary or not!
     * Make sure you know what you are doing (and provide the technical id if it's necessary).
     */
    private DomainRow createRowInner(SuperIdValues values, @Nullable Integer technicalId) {
        final var row = new DomainRow(values, technicalId, references.keySet());
        setRow(row);
        return row;
    }

    /**
     * Adds the row to the domain (by all of its ids) and to the domain by technical ids.
     */
    void setRow(DomainRow row) {
        final var ids = row.values.findAllIds(schema.ids()).foundIds();
        for (final var idValues : ids) {
            final Map<SuperIdValues, DomainRow> rowsWithSameTypeId = domain.computeIfAbsent(idValues.id(), i -> new TreeMap<>());
            rowsWithSameTypeId.put(idValues, row);
        }

        if (row.technicalId != null)
            domainByTechnicalIds.put(row.technicalId, row);
    }

    /**
     * Iteratively merges the values to the row, adding all values discovered in the process. Always returns a new row.
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

    /** The package access is just for serialization. */
    UniqueSequentialGenerator technicalIdGenerator;

    /**
     * The point of a technical id is to differentiate two rows from each other, but only if they do not have any common id that could differentiate them (or unify them, if both of them have the same value of the id).
     */
    private Integer generateTechnicalId() {
        return technicalIdGenerator.next();
    }

    /** Removes technical ids of deleted rows from the domain. */
    void removeTechnicalIds(Iterable<DomainRow> rows) {
        for (final DomainRow row : rows)
            if (row.technicalId != null)
                domainByTechnicalIds.remove(row.technicalId);
    }

    @Nullable DomainRow tryFindRowByTechnicalId(Integer technicalId) {
        return domainByTechnicalIds.get(technicalId);
    }

    public SortedSet<DomainRow> allRowsToSet() {
        final var output = new TreeSet<DomainRow>();

        for (var innerMap : domain.values())
            output.addAll(innerMap.values());

        output.addAll(domainByTechnicalIds.values());

        return output;
    }

    /**
     * Iteratively get all rows that are identified by the values (while expanding the values).
     */
    public SuperIdValues findMaximalSuperIdValues(SuperIdValues values, Set<DomainRow> outOriginalRows) {
        // First, we take all ids that can be created for this object, and we find those that can be filled from the given superId.
        // Then we find the rows that correspond to them and merge their superIds to the superId.
        // If it gets bigger, we try to generate other ids to find their objexes and so on ...

        int previousValuesSize = 0;
        Set<SuperIdValues> foundIds = new TreeSet<>();
        Set<SignatureId> notFoundIds = schema.ids().toSignatureIds();

        final var output = new SuperIdValues.Mutable(values);

        while (previousValuesSize < output.size()) {
            previousValuesSize = output.size();

            final var result = output.findAllSignatureIds(notFoundIds);
            foundIds.addAll(result.foundIds());
            notFoundIds = result.notFoundIds();

            final var foundRows = findNewRows(foundIds, outOriginalRows);
            if (foundRows.isEmpty())
                break; // We have not found anything new.

            foundRows.forEach(row -> output.add(row.values));
        }

        return output.build();
    }

    private Set<DomainRow> findNewRows(Set<SuperIdValues> foundIds, Set<DomainRow> outOriginalRows) {
        final var output = new TreeSet<DomainRow>();

        for (final var id : foundIds) {
            var row = tryFindRowById(id);
            if (row == null || outOriginalRows.contains(row))
                continue;

            outOriginalRows.add(row);
            output.add(row);
        }

        return output;
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
        // The output rows should be unique, so we have to keep them somewhere. And no, we can't use a set because it can't be queried for keys.
        final var uniqueRows = new TreeMap<DomainRow, DomainRow>();

        // TODO Use functions above to insert rows.
        // FIXME This is not correct (yet), because it doesn't use correct ids (the ids do have to change because the morphisms that form them have to change).

        // TODO How to copy signature ids?
        for (final var domainEntry : source.domain.entrySet()) {
            final SignatureId id = domainEntry.getKey();
            final Map<SuperIdValues, DomainRow> sourceRows = domainEntry.getValue();
            final Map<SuperIdValues, DomainRow> targetRows = new TreeMap<>();

            for (final DomainRow sourceRow : sourceRows.values()) {
                // TODO Pending references (probably should be empty, but we should check it).
                final DomainRow targetRow = new DomainRow(sourceRow.values, sourceRow.technicalId, Set.of());

                final DomainRow uniqueRow = uniqueRows.computeIfAbsent(targetRow, row -> row);
                targetRows.put(uniqueRow.values, uniqueRow);
            }

            if (!targetRows.isEmpty()) {
                domain.put(id, targetRows);
            }
        }

        for (final var technicalIdEntry : source.domainByTechnicalIds.entrySet()) {
            final Integer technicalId = technicalIdEntry.getKey();
            final DomainRow sourceRow = technicalIdEntry.getValue();
            final DomainRow targetRow = new DomainRow(sourceRow.values, sourceRow.technicalId, Set.of());

            final DomainRow uniqueRow = uniqueRows.computeIfAbsent(targetRow, row -> row);
            domainByTechnicalIds.put(technicalId, uniqueRow);
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
        StringBuilder builder = new StringBuilder();

        builder.append("\tKey: ").append(schema.key());
        builder.append("\n");

        builder.append("\tValues:\n");
        for (final DomainRow row : allRowsToSet())
            builder.append("\t\t").append(row).append("\n");

        return builder.toString();
    }

}
