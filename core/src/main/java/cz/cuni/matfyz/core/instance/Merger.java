package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.instance.InstanceObject.ReferenceToRow;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Max;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author jachym.bartik
 */
public class Merger {

    private final Queue<MergeRowsJob> jobs;
    private final Queue<ReferenceJob> referenceJobs;

    public Merger() {
        this.jobs = new LinkedList<>();
        this.referenceJobs = new LinkedList<>();
    }

    /**
     * Merges the row and then iterativelly merges rows from other instance objects that might be affected.
     * @param row
     * @return
     */
    public DomainRow merge(DomainRow row, InstanceObject instanceObject) {
        addMergeJob(row.superId, row.technicalIds, instanceObject);

        while (!jobs.isEmpty())
            mergePhase();

        return instanceObject.getActualRow(row);
    }

    public DomainRow mergeAlongMorphism(DomainRow domainRow, InstanceMorphism morphism) {
        // Get all mappings from the domain row for this morphism.
        var mappingsFromRow = domainRow.getMappingsFromForMorphism(morphism);
        if (morphism.schemaMorphism().max() == Max.STAR || mappingsFromRow.size() <= 1)
            return domainRow; // There is nothing to merge

        // Create a new job that merges all these rows.
        var codomainRows = new TreeSet<>(mappingsFromRow.stream().map(MappingRow::codomainRow).toList());

        addMergeJob(codomainRows, morphism.cod());

        mergePhase();

        return morphism.dom().getActualRow(domainRow);
    }

    private void mergePhase() {
        while (!jobs.isEmpty()) {
            var job = jobs.poll();
            job.process();

            // TODO make more effective:
            // - only those that are needed
            // - set, not a queue, so the same rows won't be repeated
            addReferenceJob(job.superId, job.technicalIds, job.instanceObject);
        }

        while (!referenceJobs.isEmpty()) {
            var job = referenceJobs.poll();
            job.process();
        }
    }

    private void addMergeJob(IdWithValues superId, Set<Integer> technicalId, InstanceObject instanceObject) {
        jobs.add(new MergeRowsJob(this, superId, technicalId, instanceObject));
    }

    private void addMergeJob(Set<DomainRow> rows, InstanceObject instanceObject) {
        jobs.add(new MergeRowsJob(this, InstanceObject.mergeSuperIds(rows), InstanceObject.mergeTechnicalIds(rows), instanceObject));
    }

    private void addReferenceJob(IdWithValues superId, Set<Integer> technicalIds, InstanceObject instanceObject) {
        referenceJobs.add(new ReferenceJob(this, superId, technicalIds, instanceObject));
    }

    private class MergeRowsJob {

        private final Merger merger;

        IdWithValues superId;
        Set<Integer> technicalIds;
        InstanceObject instanceObject;

        public MergeRowsJob(Merger merger, IdWithValues superId, Set<Integer> technicalIds, InstanceObject instanceObject) {
            this.merger = merger;
            this.superId = superId;
            this.technicalIds = technicalIds;
            this.instanceObject = instanceObject;
        }

        public void process() {
            Set<DomainRow> originalRows = new TreeSet<>();
            
            // Iteratively get all rows that are identified by the superId (while expanding the superId).
            // Also get all technical ids.
            var superIdOfTechnicalRows = instanceObject.findTechnicalSuperId(technicalIds, originalRows);
            superId = IdWithValues.merge(superId, superIdOfTechnicalRows);
    
            var result = instanceObject.findMaximalSuperId(superId, originalRows);
            var maximalSuperId = result.superId();
            var maximalTechnicalId = InstanceObject.mergeTechnicalIds(originalRows);
    
            // Create new Row that contains the unified superId and put it to all possible ids.
            // This also deletes the old ones.
            var newRow = instanceObject.createRow(maximalSuperId, maximalTechnicalId, result.foundIds());
    
            // Get all morphisms from and to the original rows and put the new one instead of them.
            // Detect all morphisms that have maximal cardinality ONE and merge their rows. This can cause a chain reaction.
            // This steps is done by combining the rows' superIds and then calling 
            mergeMappings(originalRows, newRow);
        }

        private void mergeMappings(Set<DomainRow> originalRows, DomainRow newRow) {
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
                    var rowSet = output.computeIfAbsent(morphism, x -> new TreeSet<>());

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

        private static void createNewMappingsForMorphism(InstanceMorphism morphism, Set<DomainRow> codomainRows, DomainRow newRow, Merger merger) {
            for (var codomainRow : codomainRows) {
                var newMapping = new MappingRow(newRow, codomainRow);
                morphism.addMapping(newMapping);
                morphism.dual().addMapping(newMapping.toDual());
            }
    
            // If there are multiple rows but the morphism allows at most one, they have to be merged as well. We do so by creating new merge job.
            if (morphism.schemaMorphism().max() == Max.ONE && codomainRows.size() > 1)
                merger.addMergeJob(codomainRows, morphism.cod());
        }

    }

    private class ReferenceJob {

        private final Merger merger;

        IdWithValues superId;
        Set<Integer> technicalIds; // The rows have to have at least some values in superId but it does not have to be a valid id ...
        InstanceObject instanceObject;

        public ReferenceJob(Merger merger, IdWithValues superId, Set<Integer> technicalIds, InstanceObject instanceObject) {
            this.merger = merger;
            this.superId = superId;
            this.technicalIds = technicalIds;
            this.instanceObject = instanceObject;
        }

        public void process() {
            var referencingRow = instanceObject.getActualRow(superId, technicalIds);

            for (var pair : referencingRow.getAndRemovePendingReferencePairs())
                for (var reference : instanceObject.getReferencesForSignature(pair.signature()))
                    sendReferences(referencingRow, reference, pair.value());
        }

        private void sendReferences(DomainRow domainRow, ReferenceToRow reference, String value) {
            var targetRows = domainRow.traverseThrough(reference.path);

            for (var targetRow : targetRows) {
                if (!targetRow.hasSignature(reference.signatureInOther))
                    continue; // The row already has the value.

                // Add value to the targetRow.
                var builder = new IdWithValues.Builder();
                builder.add(targetRow.superId);
                builder.add(reference.signatureInOther, value);

                merger.addMergeJob(builder.build(), targetRow.technicalIds, instanceObject);
            }
        }

    }

}
