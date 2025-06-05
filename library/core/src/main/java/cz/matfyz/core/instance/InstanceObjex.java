package cz.matfyz.core.instance;

import cz.matfyz.core.exception.ObjexException;
import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.schema.SchemaCategory.SchemaPath;
import cz.matfyz.core.utils.UniqueSequentialGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Each object from instance category is modeled as a set of tuples ({@link DomainRow}).
 */
public class InstanceObjex implements Identified<InstanceObjex, Key> {

    public final SchemaObjex schema;

    private final InstanceCategory instance;
    private final Map<SignatureId, Map<SuperIdWithValues, DomainRow>> domain = new TreeMap<>();
    private final Map<String, DomainRow> domainByTechnicalIds = new TreeMap<>();

    public InstanceObjex(SchemaObjex schema, InstanceCategory instance) {
        this.schema = schema;
        this.instance = instance;
        this.technicalIdGenerator = UniqueSequentialGenerator.create();
    }

    public boolean isEmpty() {
        return domain.isEmpty() && domainByTechnicalIds.isEmpty();
    }

    // TODO rozlišit id od superId
    private DomainRow getRowById(SuperIdWithValues id) {
        Map<SuperIdWithValues, DomainRow> rowsWithSameTypeId = domain.get(id.id());
        return rowsWithSameTypeId == null ? null : rowsWithSameTypeId.get(id);
    }

    private DomainRow getRowByTechnicalId(String technicalId) {
        return domainByTechnicalIds.get(technicalId);
    }

    public void setRow(DomainRow row, Collection<SuperIdWithValues> ids) {
        for (var id : ids) {
            Map<SuperIdWithValues, DomainRow> rowsWithSameTypeId = domain.get(id.id());
            if (rowsWithSameTypeId == null) {
                rowsWithSameTypeId = new TreeMap<>();
                domain.put(id.id(), rowsWithSameTypeId);
            }
            rowsWithSameTypeId.put(id, row);
        }

        for (var technicalId : row.technicalIds)
            domainByTechnicalIds.put(technicalId, row);
    }

    public DomainRow getOrCreateRow(SuperIdWithValues superId) {
        // If the superId doesn't contain any id, we have to create a technical one.
        if (superId.findFirstId(schema.ids()) == null)
            return createRow(superId, Set.of(generateTechnicalId()), Set.of());

        final var merger = new Merger(instance);
        return merger.merge(superId, this);
    }

    public static DomainRow getOrCreateRowWithEdge(InstanceCategory instance, SuperIdWithValues superId, DomainRow parent, SchemaEdge edgeFromParent) {
        final var merger = new Merger(instance);
        return merger.merge(superId, parent, edgeFromParent);
    }

    DomainRow createRow(SuperIdWithValues superId) {
        Set<String> technicalIds = superId.findFirstId(schema.ids()) == null
            ? Set.of(generateTechnicalId())
            : Set.of();
        final var ids = superId.findAllIds(schema.ids()).foundIds();

        return createRow(superId, technicalIds, ids);
    }

    DomainRow createRow(SuperIdWithValues superId, Set<String> technicalIds, Set<SuperIdWithValues> allIds) {
        // TODO this can be optimized - we can discard the references that were referenced in all merged rows.
        // However, it might be quite rare, so the overhead caused by finding such ids would be greater than the savings.
        final var row = new DomainRow(superId, technicalIds, referencesToRows.keySet());
        setRow(row, allIds);

        return row;
    }

    public DomainRow getRow(SuperIdWithValues superId) {
        for (final var id : superId.findAllIds(schema.ids()).foundIds()) {
            final var row = getRowById(id);
            if (row != null)
                return row;
        }

        return null;
    }

    // TODO fix - problém je, že getRowById může stále vracet null - tedy toto je použitelné jenom když víme, že alespoň nějaká část superId už musí být zmergovaná, a proto by se nejspíš měla používat jen ta funkce podtím
    /**
     * Returns the most recent row for the superId or technicalIds.
     */
    public DomainRow getActualRow(SuperIdWithValues superId, Set<String> technicalIds) {
        // Simply find the first id of all possible ids (any of them should point to the same row).
        var foundId = superId.findFirstId(schema.ids());
        if (foundId != null)
            return getRowById(foundId);

        // Then the row has to be identified by its technical ids (again, any of them will do).
        var technicalId = technicalIds.stream().findFirst();
        if (technicalId.isPresent())
            return getRowByTechnicalId(technicalId.get());

        throw ObjexException.actualRowNotFound(superId, technicalIds);
    }

    /**
     * Returns the most recent value of the row (possibly the inpuct object).
     */
    public DomainRow getActualRow(DomainRow row) {
        return getActualRow(row.superId, row.technicalIds);
    }

    /** The package access is just for serialization. */
    UniqueSequentialGenerator technicalIdGenerator;

    /**
     * The point of a technical id is to differentiate two rows from each other, but only if they do not have any common id that could differentiate them (or unify them, if both of them have the same value of the id).
     */
    private String generateTechnicalId() {
        return "#" + technicalIdGenerator.next();
    }

    public SortedSet<DomainRow> allRowsToSet() {
        final var output = new TreeSet<DomainRow>();

        for (var innerMap : domain.values())
            output.addAll(innerMap.values());

        output.addAll(domainByTechnicalIds.values());

        return output;
    }

    public SuperIdWithValues findTechnicalSuperId(Set<String> technicalIds, Set<DomainRow> outOriginalRows) {
        final var builder = new SuperIdWithValues.Builder();

        for (final var technicalId : technicalIds) {
            final var row = getRowByTechnicalId(technicalId);
            if (!outOriginalRows.contains(row)) {
                outOriginalRows.add(row);
                builder.add(row.superId);
            }
        }

        return builder.build();
    }

    public record FindSuperIdResult(SuperIdWithValues superId, Set<SuperIdWithValues> foundIds) {}

    /**
     * Iteratively get all rows that are identified by the superId (while expanding the superId).
     * @param superId
     * @return
     */
    public FindSuperIdResult findMaximalSuperId(SuperIdWithValues superId, Set<DomainRow> outOriginalRows) {
        // First, we take all ids that can be created for this object, and we find those, that can be filled from the given superId.
        // Then we find the rows that correspond to them and merge their superIds to the superId.
        // If it gets bigger, we try to generate other ids to find their objexes and so on ...

        int previousSuperIdSize = 0;
        Set<SuperIdWithValues> foundIds = new TreeSet<>();
        Set<SignatureId> notFoundIds = schema.ids().toSignatureIds();

        while (previousSuperIdSize < superId.size()) {
            previousSuperIdSize = superId.size();

            final var result = superId.findAllSignatureIds(notFoundIds);
            foundIds.addAll(result.foundIds());
            notFoundIds = result.notFoundIds();

            final var foundRows = findNewRows(foundIds, outOriginalRows);
            if (foundRows.isEmpty())
                break; // We have not found anything new.

            superId = SuperIdWithValues.merge(superId, mergeSuperIds(foundRows));
        }

        return new FindSuperIdResult(superId, foundIds);
    }

    private static SuperIdWithValues mergeSuperIds(Collection<DomainRow> rows) {
        final var builder = new SuperIdWithValues.Builder();
        rows.forEach(row -> builder.add(row.superId));

        return builder.build();
    }

    private Set<DomainRow> findNewRows(Set<SuperIdWithValues> foundIds, Set<DomainRow> outOriginalRows) {
        final var output = new TreeSet<DomainRow>();

        for (var id : foundIds) {
            var row = getRowById(id);
            if (row == null || outOriginalRows.contains(row))
                continue;

            outOriginalRows.add(row);
            output.add(row);
        }

        return output;
    }

    private final Map<Signature, Set<ReferenceToRow>> referencesToRows = new TreeMap<>();

    public void addReferenceToRow(Signature signatureInThis, SchemaPath path, Signature signatureInOther) {
        final var referencesForSignature = referencesToRows.computeIfAbsent(signatureInThis, x -> new TreeSet<>());
        referencesForSignature.add(new ReferenceToRow(signatureInThis, path, signatureInOther));
    }

    public Set<ReferenceToRow> getReferencesForSignature(Signature signatureInThis) {
        return referencesToRows.get(signatureInThis);
    }

    public static class ReferenceToRow implements Comparable<ReferenceToRow> {

        public final Signature signatureInThis;
        public final SchemaPath path;
        public final Signature signatureInOther;

        public ReferenceToRow(Signature signatureInThis, SchemaPath path, Signature signatureInOther) {
            this.signatureInThis = signatureInThis;
            this.path = path;
            this.signatureInOther = signatureInOther;
        }

        @Override public boolean equals(Object object) {
            if (this == object)
                return true;

            return object instanceof ReferenceToRow reference
                && signatureInThis.equals(reference.signatureInThis)
                && path.equals(reference.path)
                && signatureInOther.equals(reference.signatureInOther);
        }

        @Override public int compareTo(ReferenceToRow reference) {
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
            final Map<SuperIdWithValues, DomainRow> sourceRows = domainEntry.getValue();
            final Map<SuperIdWithValues, DomainRow> targetRows = new TreeMap<>();

            for (final DomainRow sourceRow : sourceRows.values()) {
                // TODO Pending references (probably should be empty, but we should check it).
                final DomainRow targetRow = new DomainRow(sourceRow.superId, sourceRow.technicalIds, Set.of());

                final DomainRow uniqueRow = uniqueRows.computeIfAbsent(targetRow, row -> row);
                targetRows.put(uniqueRow.superId, uniqueRow);
            }

            if (!targetRows.isEmpty()) {
                domain.put(id, targetRows);
            }
        }

        for (final var technicalIdEntry : source.domainByTechnicalIds.entrySet()) {
            final String technicalId = technicalIdEntry.getKey();
            final DomainRow sourceRow = technicalIdEntry.getValue();
            final DomainRow targetRow = new DomainRow(sourceRow.superId, sourceRow.technicalIds, Set.of());

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
