package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Max;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Each object from instance category is modeled as a set of tuples ({@link DomainRow}).
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceObject implements Serializable, CategoricalObject, JSONConvertible {

    private final SchemaObject schemaObject;
    private final Map<Id, Map<IdWithValues, DomainRow>> domain = new TreeMap<>();
    private final Map<Integer, DomainRow> domainByTechnicalIds = new TreeMap<>();
    
    public Map<Id, Map<IdWithValues, DomainRow>> domain() {
        return domain;
    }

    public DomainRow getRowById(IdWithValues id) {
        Map<IdWithValues, DomainRow> rowsWithSameTypeId = domain.get(id.id());
        return rowsWithSameTypeId == null ? null : rowsWithSameTypeId.get(id);
    }

    public DomainRow getRowByTechnicalId(Integer technicalId) {
        return domainByTechnicalIds.get(technicalId);
    }

    private void setRow(DomainRow row, Iterable<IdWithValues> ids) {
        for (var id : ids) {
            Map<IdWithValues, DomainRow> rowsWithSameTypeId = domain.get(id.id());
            if (rowsWithSameTypeId == null) {
                rowsWithSameTypeId = new TreeMap<>();
                domain.put(id.id(), rowsWithSameTypeId);
            }
            rowsWithSameTypeId.put(id, row);
        }

        for (var technicalId : row.technicalIds)
            domainByTechnicalIds.put(technicalId, row);
    }

    private void setRow(DomainRow row) {
        var ids = findIdsInSuperId(row.superId, schemaObject.ids()).getValue0();
        setRow(row, ids);
    }

    /**
     * Returns the most recent value of the row (possibly the inpuct object).
     * @param row
     * @return
     */
    public DomainRow getActualRow(DomainRow row) {
        // Simple search by ids, any of them will do.
        var ids = findIdsInSuperId(row.superId, schemaObject.ids()).getValue0();
        for (var id : ids) {
            var foundRow = getRowById(id);
            if (foundRow != null)
                return foundRow;
        }

        var technicalId = row.technicalIds.stream().findFirst();
        if (technicalId.isPresent())
            return getRowByTechnicalId(technicalId.get());

        // This should not happen.
        throw new UnsupportedOperationException("Actual row not found for id: " + row.superId + ".");
    }

    public InstanceObject(SchemaObject schemaObject) {
        this.schemaObject = schemaObject;
    }

    // The point of a technical id is to differentiate two idWithValues from each other, but only if they do not share any other id.
    private int lastTechnicalId = 0;

    public int generateTechnicalId() {
        lastTechnicalId++;
        return lastTechnicalId;
    }
    
    public Key key() {
        return schemaObject.key();
    }
    
    public SchemaObject schemaObject() {
        return schemaObject;
    }

    public List<DomainRow> rows() {
        var output = new ArrayList<DomainRow>();

        for (var innerMap : domain.values())
            for (DomainRow row : innerMap.values())
                output.add(row);

        return output;
    }

    public void addRow(DomainRow row) {
        setRow(row);
    }

    public DomainRow mergeAlongMorphism(DomainRow domainRow, InstanceMorphism morphism) {
        var merger = new Merger();

        // Get all mappings from the domain row for this morphism.
        var mappingsFromRow = domainRow.getMappingsFromForMorphism(morphism);
        if (morphism.schemaMorphism().max() == Max.STAR || mappingsFromRow.size() <= 1)
            return domainRow; // There is nothing to merge

        // Create a new job that merges all these rows.
        var codomainRows = new TreeSet<>(mappingsFromRow.stream().map(mapping -> mapping.codomainRow()).toList());


        /* TODO change */
        //merger.add(new Merger.MergeJob(codomainRows, morphism.cod()));
        merger.addMergeJob(codomainRows, morphism.cod());
        merger.mergePhase();
        /*
        while (!merger.isEmpty()) {
            var job = merger.poll();
            job.instanceObject.merge(job.superId, job.technicalIds, merger);
        }
         */
        /* TODO change */

        return getActualRow(domainRow);
    }

    public void merge(IdWithValues superId, Set<Integer> technicalIds, Merger merger) {
        // Iteratively get all rows that are identified by the superId (while expanding the superId).
        Set<DomainRow> originalRows = new TreeSet<>();
        Set<IdWithValues> allIds = new TreeSet<>();

        var superIdOfTechnicalRows = findTechnicalSuperId(technicalIds, originalRows);
        superId = IdWithValues.merge(superId, superIdOfTechnicalRows);

        var maximalSuperId = findMaximalSuperId(superId, originalRows, allIds);
        var maximalTechnicalId = mergeTechnicalIds(originalRows);

        // Create new Row that contains the unified superId and put it to all possible ids.
        // This also deletes the old ones.
        var newRow = new DomainRow(maximalSuperId, maximalTechnicalId, this); //, allIds);
        setRow(newRow, allIds);

        // Get all morphisms from and to the original rows and put the new one instead of them.
        // Detect all morphisms that have maximal cardinality ONE and merge their rows. This can cause a chain reaction.
        // This steps is done by combining the rows' superIds and then calling 
        mergeMappings(originalRows, newRow, merger);
        //mergeMappingsFrom(originalRows, newRow, merger);
        //mergeMappingsTo(originalRows, newRow, merger);
    }

    private IdWithValues findTechnicalSuperId(Set<Integer> technicalIds, Set<DomainRow> outOriginalRows) {
        var builder = new IdWithValues.Builder();

        for (var technicalId : technicalIds) {
            var row = getRowByTechnicalId(technicalId);
            if (!outOriginalRows.contains(row)) {
                outOriginalRows.add(row);
                builder.add(row.superId);
            }
        }

        return builder.build();
    }

    /**
     * Iteratively get all rows that are identified by the superId (while expanding the superId).
     * @param superId
     * @return
     */
    public IdWithValues findMaximalSuperId(IdWithValues superId, Set<DomainRow> outOriginalRows, Set<IdWithValues> outAllIds) {
        // First, we take all ids that can be created for this object, and we find those, that can be filled from the given superId.
        // Then we find the rows that correspond to them and merge their superIds to the superId.
        // If it gets bigger, we try to generate other ids to find their objects and so on ...

        int previousSuperIdSize = 0;
        Set<Id> notFoundIds = schemaObject.ids();

        while (previousSuperIdSize < superId.size()) {
            previousSuperIdSize = superId.size();

            var result = findIdsInSuperId(superId, notFoundIds);
            var foundIds = result.getValue0();
            notFoundIds = result.getValue1();

            outAllIds.addAll(foundIds);
            
            var foundRows = findNewRows(foundIds, outOriginalRows);
            if (foundRows.isEmpty())
                break; // We have not found anything new.

            superId = IdWithValues.merge(superId, mergeSuperIds(foundRows));
        }

        return superId;
    }

    public static IdWithValues mergeSuperIds(Iterable<DomainRow> rows) {
        var builder = new IdWithValues.Builder();

        for (var row : rows)
            builder.add(row.superId);
        
        return builder.build();
    }

    public static Set<Integer> mergeTechnicalIds(Iterable<DomainRow> rows) {
        var output = new TreeSet<Integer>();
        rows.forEach(row -> output.addAll(row.technicalIds));
        return output;
    }

    private Set<DomainRow> findNewRows(Set<IdWithValues> foundIds, Set<DomainRow> outOriginalRows) {
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

    //private void mergeMappingsFrom(Set<DomainRow> originalRows, DomainRow newRow, Merger merger) {
    private void mergeMappings(Set<DomainRow> originalRows, DomainRow newRow, Merger merger) {
        // First, we find all mappings that go from the old rows to other rows, remove them, and get their codomain rows, sorted by their morphisms.
        Map<InstanceMorphism, Set<DomainRow>> codomainRowsForAllMorphisms = findAndRemoveMorphismsFromOriginalRows(originalRows);

        // We have to create only one mapping for each unique pair (newRow, rowTo), hence the rows to are stored in a set.
        for (var entry : codomainRowsForAllMorphisms.entrySet()) {
            var morphism = entry.getKey();
            var codomainRows = entry.getValue();
            createNewMappingsForMorphism(morphism, codomainRows, newRow, merger);
        }
    }

    /**
     * Find and remove all mappings that go from the original rows to other rows. The rows to which the mappings pointed are returned, sorted by the morphisms of the mappings.
     * @param originalRows
     * @return
     */
    private Map<InstanceMorphism, Set<DomainRow>> findAndRemoveMorphismsFromOriginalRows(Set<DomainRow> originalRows) {
        Map<InstanceMorphism, Set<DomainRow>> output = new TreeMap<>();

        for (var row : originalRows) {
            for (var entry : row.getAllMappingsFrom()) {
                var morphism = entry.getKey();
                var rowSet = getOrCreateRowSet(output, morphism);
                for (var mappingRow : entry.getValue()) {
                    rowSet.add(mappingRow.codomainRow());
                    
                    // Remove old mappings from their rows.
                    morphism.removeMapping(mappingRow);
                    morphism.dual().removeMapping(mappingRow.toDual());
                }
            }
        }

        return output;
    }

    private void createNewMappingsForMorphism(InstanceMorphism morphism, Set<DomainRow> codomainRows, DomainRow newRow, Merger merger) {
        for (var codomainRow : codomainRows) {
            var newMapping = new MappingRow(newRow, codomainRow);
            morphism.addMapping(newMapping);
            morphism.dual().addMapping(newMapping.toDual());
        }

        // If there are multiple rows but the morphism allows at most one, they have to be merged as well.
        // We do so by creating new merge job.
        if (morphism.schemaMorphism().max() == Max.ONE && codomainRows.size() > 1) {
            //merger.add(new Merger.MergeJob(codomainRows, morphism.cod()));
            merger.addMergeJob(codomainRows, morphism.cod());
        }
    }

    private Set<DomainRow> getOrCreateRowSet(Map<InstanceMorphism, Set<DomainRow>> map, InstanceMorphism morphism) {
        var set = map.computeIfAbsent(morphism, x -> new TreeSet<>());
        return set;
    }

    /**
     * Returns all ids that are contained in given superId as a subset.
     * @param superId
     * @return
     */
    // TODO private
    //private Pair<Set<IdWithValues>, Set<Id>> findIdsInSuperId(IdWithValues superId, Set<Id> idsToFind) {
    public Pair<Set<IdWithValues>, Set<Id>> findIdsInSuperId(IdWithValues superId, Set<Id> idsToFind) {
        var foundIds = new TreeSet<IdWithValues>();
        var notFoundIds = new TreeSet<Id>();

        // For each possible id from ids, we find if superId contains all its signatures (i.e., if superId.signatures() is a superset of id.signatures()).
        for (Id id : idsToFind) {
            var builder = new IdWithValues.Builder();
            boolean idIsInSuperId = true;

            for (var signature : id.signatures()) {
                String value = superId.map().get(signature);
                if (value == null) {
                    idIsInSuperId = false;
                    break;
                }

                builder.add(signature, value);
            }
            // If so, we add the id (with its corresponding values) to the output.
            if (idIsInSuperId)
                foundIds.add(builder.build());
            else
                notFoundIds.add(id);
        }

        return new Pair<>(foundIds, notFoundIds);
    }

    // TODO change name
    public static class NoteToOtherRow {

        public final Signature signature;
        public final List<InstanceMorphism> path;
        public final Signature signatureInOther;

        public NoteToOtherRow(Signature signature, List<InstanceMorphism> path, Signature signatureInOther) {
            this.signature = signature;
            this.path = path;
            this.signatureInOther = signatureInOther;
        }

    }

    private final Map<Signature, List<NoteToOtherRow>> pathsToOtherSuperIds = new TreeMap<>();

    public void addPathToSuperId(Signature signature, List<InstanceMorphism> path, Signature signatureInOther) {
        //var listForSignature = pathsToOtherSuperIds.get(signature);
        var listForSignature = pathsToOtherSuperIds.computeIfAbsent(signature, x -> new ArrayList<>());
        listForSignature.add(new NoteToOtherRow(signature, path, signatureInOther));
    }

    /**
     * Notify all relevant rows about the new values from the superId of the row.
     * @param domainRow
     */
    public void notifyOtherSuperIds(DomainRow domainRow, Merger merger) {
        for (var signature : domainRow.unnotifiedSignatures) {
            if (!domainRow.hasSignature(signature))
                continue; // The value for this signature is not known to the row.

            var notes = pathsToOtherSuperIds.get(signature);
            if (notes == null)
                continue; // The signature is not wanted by any other row.

            domainRow.unnotifiedSignatures.remove(signature);

            var value = domainRow.getValue(signature);

            for (var note : notes) {
                var targetRows = domainRow.traverseThrough(note.path);
                for (var targetRow : targetRows) {
                    if (!targetRow.hasSignature(signature))
                        continue; // The row already has the value.

                    // Add value to the targetRow.
                    var builder = new IdWithValues.Builder();
                    builder.add(targetRow.superId);
                    builder.add(note.signatureInOther, value);
                    merger.addMergeJob(builder.build(), targetRow.technicalIds, this);
                }
            }
        }
    }
    
    @Override
    public int objectId() {
        return key().getValue();
    }

    @Override
    public int compareTo(CategoricalObject categoricalObject) {
        return objectId() - categoricalObject.objectId();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\tKey: ").append(key()).append("\n");
        builder.append("\tValues:\n");
        // Consistent ordering of the keys for testing purposes.
        for (Id id : new TreeSet<>(domain.keySet())) {
            var subdomain = domain.get(id);
            // Again, ordering.
            for (IdWithValues idWithValues : new TreeSet<>(subdomain.keySet()))
                builder.append("\t\t").append(subdomain.get(idWithValues)).append("\n");
        }
        
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof InstanceObject instanceObject && schemaObject.equals(instanceObject.schemaObject);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<InstanceObject> {

        @Override
        protected JSONObject innerToJSON(InstanceObject object) throws JSONException {
            var output = new JSONObject();
    
            output.put("key", object.key().toJSON());

            var domain = object.rows().stream().map(row -> row.toJSON()).toList();
            output.put("domain", new JSONArray(domain));
            
            return output;
        }
    
    }

}
