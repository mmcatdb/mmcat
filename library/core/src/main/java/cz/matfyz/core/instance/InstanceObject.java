package cz.matfyz.core.instance;

import cz.matfyz.core.category.CategoricalObject;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.exception.ObjectException;
import cz.matfyz.core.instance.InstanceCategory.InstanceEdge;
import cz.matfyz.core.instance.InstanceCategory.InstancePath;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Each object from instance category is modeled as a set of tuples ({@link DomainRow}).
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceObject implements CategoricalObject {

    public final SchemaObject schemaObject;
    private final Map<SignatureId, Map<SuperIdWithValues, DomainRow>> domain = new TreeMap<>();
    private final Map<String, DomainRow> domainByTechnicalIds = new TreeMap<>();

    public InstanceObject(SchemaObject schemaObject) {
        this.schemaObject = schemaObject;
    }

    /**
     * Copies all data from the source instance object to this object. All previous data is discareded!
     */
    public void load(InstanceObject source) {
        domain.clear();
        source.domain.entrySet().forEach(entry -> {
            // Both SuperIdWithValues and DomainRow are immutable, so we just need to copy the map.
            // TODO - this is not true for the pending references. But that is not an issue (yet).
            var newRowsWithSameId = new TreeMap<>(entry.getValue());
            domain.put(entry.getKey(), newRowsWithSameId);
        });

        domainByTechnicalIds.clear();
        domainByTechnicalIds.putAll(source.domainByTechnicalIds);
    }

    public Key key() {
        return schemaObject.key();
    }

    public String label() {
        return schemaObject.label();
    }

    public SignatureId superId() {
        return schemaObject.superId();
    }

    /**
     * Immutable.
     */
    public ObjectIds ids() {
        return schemaObject.ids();
    }

    public boolean isEmpty() {
        return domain.isEmpty() && domainByTechnicalIds.isEmpty();
    }

    // TODO rozlišit id od superId
    public DomainRow getRowById(SuperIdWithValues id) {
        Map<SuperIdWithValues, DomainRow> rowsWithSameTypeId = domain.get(id.id());
        return rowsWithSameTypeId == null ? null : rowsWithSameTypeId.get(id);
    }

    private DomainRow getRowByTechnicalId(String technicalId) {
        return domainByTechnicalIds.get(technicalId);
    }

    private void setRow(DomainRow row, Collection<SuperIdWithValues> ids) {
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
        if (superId.findFirstId(ids()) == null)
            return createRow(superId, Set.of(generateTechnicalId()), Set.of());

        var merger = new Merger();
        return merger.merge(superId, this);
    }

    public static DomainRow getOrCreateRowWithEdge(SuperIdWithValues superId, DomainRow parent, InstanceEdge edgeFromParent) {
        var merger = new Merger();
        return merger.merge(superId, parent, edgeFromParent);
    }

    public static DomainRow getOrCreateRowWithBaseMorphism(SuperIdWithValues superId, DomainRow parent, InstanceMorphism baseMorphismFromParent) {
        return getOrCreateRowWithEdge(superId, parent, new InstanceEdge(baseMorphismFromParent, true));
    }

    public static DomainRow connectRowWithBaseMorphism(DomainRow domainRow, DomainRow parent, InstanceMorphism baseMorphismFromParent) {
        // TODO optimize
        return getOrCreateRowWithBaseMorphism(domainRow.superId, parent, baseMorphismFromParent);
    }

    DomainRow createRow(SuperIdWithValues superId) {
        Set<String> technicalIds = superId.findFirstId(ids()) == null
            ? Set.of(generateTechnicalId())
            : Set.of();
        var ids = superId.findAllIds(ids()).foundIds();

        return createRow(superId, technicalIds, ids);
    }

    DomainRow createRow(SuperIdWithValues superId, Set<String> technicalIds, Set<SuperIdWithValues> allIds) {
        // TODO this can be optimized - we can discard the references that were referenced in all merged rows.
        // However, it might be quite rare, so the overhead caused by finding such ids would be greater than the savings.
        var row = new DomainRow(superId, technicalIds, referencesToRows.keySet());
        setRow(row, allIds);

        return row;
    }

    public DomainRow getRow(SuperIdWithValues superId) {
        for (var id : superId.findAllIds(ids()).foundIds()) {
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
        var foundId = superId.findFirstId(ids());
        if (foundId != null)
            return getRowById(foundId);

        // Then the row has to be identified by its technical ids (again, any of them will do).
        var technicalId = technicalIds.stream().findFirst();
        if (technicalId.isPresent())
            return getRowByTechnicalId(technicalId.get());

        throw ObjectException.actualRowNotFound(superId, technicalIds);
    }

    /**
     * Returns the most recent value of the row (possibly the inpuct object).
     */
    public DomainRow getActualRow(DomainRow row) {
        return getActualRow(row.superId, row.technicalIds);
    }

    // The point of a technical id is to differentiate two rows from each other, but only if they do not have any common id that could differentiate them (or unify them, if both of them have the same value of the id).
    private int lastTechnicalId = 0;

    public String generateTechnicalId() {
        lastTechnicalId++;
        return "#" + lastTechnicalId;
    }

    public SortedSet<DomainRow> allRowsToSet() {
        var output = new TreeSet<DomainRow>();

        for (var innerMap : domain.values())
            output.addAll(innerMap.values());

        output.addAll(domainByTechnicalIds.values());

        return output;
    }

    public SuperIdWithValues findTechnicalSuperId(Set<String> technicalIds, Set<DomainRow> outOriginalRows) {
        var builder = new SuperIdWithValues.Builder();

        for (var technicalId : technicalIds) {
            var row = getRowByTechnicalId(technicalId);
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
        // If it gets bigger, we try to generate other ids to find their objects and so on ...

        int previousSuperIdSize = 0;
        Set<SuperIdWithValues> foundIds = new TreeSet<>();
        Set<SignatureId> notFoundIds = ids().toSignatureIds();

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

    public static SuperIdWithValues mergeSuperIds(Collection<DomainRow> rows) {
        var builder = new SuperIdWithValues.Builder();

        for (var row : rows)
            builder.add(row.superId);

        return builder.build();
    }

    public static Set<String> mergeTechnicalIds(Collection<DomainRow> rows) {
        var output = new TreeSet<String>();
        rows.forEach(row -> output.addAll(row.technicalIds));
        return output;
    }

    private Set<DomainRow> findNewRows(Set<SuperIdWithValues> foundIds, Set<DomainRow> outOriginalRows) {
        var output = new TreeSet<DomainRow>();

        for (var id : foundIds) {
            var row = getRowById(id);
            if (row == null || outOriginalRows.contains(row))
                continue;

            outOriginalRows.add(row);
            output.add(row);
        }

        return output;
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\tKey: ").append(key());
        if (!label().isEmpty())
            builder.append("\t(").append(label()).append(")");
        builder.append("\n");

        builder.append("\tValues:\n");
        for (final DomainRow row : allRowsToSet())
            builder.append("\t\t").append(row).append("\n");

        return builder.toString();
    }

    @Override public boolean equals(Object object) {
        return object instanceof InstanceObject instanceObject && schemaObject.equals(instanceObject.schemaObject);
    }

    private final Map<Signature, Set<ReferenceToRow>> referencesToRows = new TreeMap<>();

    public void addReferenceToRow(Signature signatureInThis, InstancePath path, Signature signatureInOther) {
        var referencesForSignature = referencesToRows.computeIfAbsent(signatureInThis, x -> new TreeSet<>());
        referencesForSignature.add(new ReferenceToRow(signatureInThis, path, signatureInOther));
    }

    public Set<ReferenceToRow> getReferencesForSignature(Signature signatureInThis) {
        return referencesToRows.get(signatureInThis);
    }

    public static class ReferenceToRow implements Comparable<ReferenceToRow> {

        public final Signature signatureInThis;
        public final InstancePath path;
        public final Signature signatureInOther;

        public ReferenceToRow(Signature signatureInThis, InstancePath path, Signature signatureInOther) {
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

            var x1 = signatureInThis.compareTo(reference.signatureInThis);
            if (x1 != 0)
                return x1;

            var x2 = path.signature().compareTo(reference.path.signature());
            if (x2 != 0)
                return x2;

            return signatureInOther.compareTo(reference.signatureInOther);
        }

    }

}
