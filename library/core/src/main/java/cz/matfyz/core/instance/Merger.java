package cz.matfyz.core.instance;

import cz.matfyz.core.instance.InstanceCategory.InstanceEdge;
import cz.matfyz.core.instance.InstanceObject.ReferenceToRow;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Merger {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(Merger.class);

    private final Queue<MergeRowsJob> jobs;
    private final Queue<ReferenceJob> referenceJobs;

    Merger() {
        this.jobs = new ArrayDeque<>();
        this.referenceJobs = new ArrayDeque<>();
    }

    private void processQueues() {
        while (!jobs.isEmpty())
            mergePhase();
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

    /**
     * Merges the row and then iterativelly merges rows from other instance objects that might be affected.
     *
     * @param superId The super id must contain at least one id.
     */
    public DomainRow merge(SuperIdWithValues superId, InstanceObject instanceObject) {
        addMergeJob(superId, Set.of(), instanceObject);
        processQueues();

        return instanceObject.getActualRow(superId, Set.of());
    }

    /**
     * @param superId SuperId of the new row we want to create.
     * @param parent Parent row to which we should connect the new row.
     * @param edge Edge from parent to the new row.
     * @return
     */
    public DomainRow merge(SuperIdWithValues superId, DomainRow parent, InstanceEdge edge) {
        final var childObject = edge.to();

        // First, we try to find the row by the superId.
        var currentRow = childObject.getRow(superId);
        if (currentRow != null)
            return addToRowAndConnect(currentRow, superId, parent, edge);

        // Then we try to find it by the connection.
        if (!edge.isArray()) {
            var mapping = parent.getMappingsForEdge(edge).stream().findFirst();
            if (mapping.isPresent())
                return addToRow(mapping.get().codomainRow(), superId, edge.to());
        }

        // No such row exists yet, so we have to create it. It also cannot be merged so we are not doing that.
        var newRow = childObject.createRow(superId);
        edge.createMapping(parent, newRow);

        addReferenceJob(newRow.superId, newRow.technicalIds, childObject);
        processQueues();

        return newRow;
    }

    private DomainRow addToRowAndConnect(DomainRow currentRow, SuperIdWithValues superId, DomainRow parent, InstanceEdge edge) {
        // TODO more effective search, e.g., map.
        for (final var codomainRow : parent.getCodomainForEdge(edge))
            if (codomainRow.equals(currentRow))
                return addToRow(currentRow, superId, edge.to()); // The connection already exists so we just have to add to the superId.

        // The connection does not exist yet, so we create it and then merge it.
        // TODO optimization - merging with the knowledge of the connection, so we would not have create it, then delete it and then create it for the new row.
        edge.createMapping(parent, currentRow);
        return addToRow(currentRow, superId, edge.to());
    }

    /**
     * Add information from the superId to the existing row.
     */
    private DomainRow addToRow(DomainRow currentRow, SuperIdWithValues superId, InstanceObject object) {
        var newSuperId = SuperIdWithValues.merge(currentRow.superId, superId);
        if (newSuperId.size() == currentRow.superId.size())
            return currentRow; // The row already contains everything from the merging superId.

        return merge(superId, object);
    }

    /*
    public DomainRow mergeAlongMorphism(DomainRow domainRow, InstanceMorphism morphism) {
        // Get all mappings from the domain row for this morphism.
        var mappingsFromRow = domainRow.getMappingsFromForMorphism(morphism);
        if (morphism.isArray() || mappingsFromRow.size() <= 1)
            return domainRow; // There is nothing to merge

        // Create a new job that merges all these rows.
        var codomainRows = new TreeSet<>(mappingsFromRow.stream().map(MappingRow::codomainRow).toList());

        addMergeJob(codomainRows, morphism.cod());
        processQueues();

        return morphism.dom().getActualRow(domainRow);
    }
    */

    private void addMergeJob(SuperIdWithValues superId, Set<String> technicalId, InstanceObject instanceObject) {
        jobs.add(new MergeRowsJob(this, superId, technicalId, instanceObject));
    }

    // private void addMergeJob(Set<DomainRow> rows, InstanceObject instanceObject) {
    //     jobs.add(new MergeRowsJob(this, InstanceObject.mergeSuperIds(rows), InstanceObject.mergeTechnicalIds(rows), instanceObject));
    // }

    private void addReferenceJob(SuperIdWithValues superId, Set<String> technicalIds, InstanceObject instanceObject) {
        referenceJobs.add(new ReferenceJob(this, superId, technicalIds, instanceObject));
    }

    private class MergeRowsJob {

        private final Merger merger;

        SuperIdWithValues superId;
        Set<String> technicalIds;
        InstanceObject instanceObject;

        MergeRowsJob(Merger merger, SuperIdWithValues superId, Set<String> technicalIds, InstanceObject instanceObject) {
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
            superId = SuperIdWithValues.merge(superId, superIdOfTechnicalRows);

            var result = instanceObject.findMaximalSuperId(superId, originalRows);
            var maximalSuperId = result.superId();
            var maximalTechnicalId = mergeTechnicalIds(originalRows);

            if (originalRows.size() == 1)
                return; // No merging is required

            // Create new Row that contains the unified superId and put it to all possible ids.
            // This also deletes the old ones.
            var newRow = instanceObject.createRow(maximalSuperId, maximalTechnicalId, result.foundIds());

            // Get all morphisms from and to the original rows and put the new one instead of them.
            // Detect all morphisms that have maximal cardinality ONE and merge their rows. This can cause a chain reaction.
            // This steps is done by combining the rows' superIds and then calling merge.
            mergeMappings(originalRows, newRow);
        }

        private static Set<String> mergeTechnicalIds(Collection<DomainRow> rows) {
            final var output = new TreeSet<String>();
            rows.forEach(row -> output.addAll(row.technicalIds));

            return output;
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
                    }
                }
            }

            return output;
        }

        private static void createNewMappingsForMorphism(InstanceMorphism morphism, Set<DomainRow> codomainRows, DomainRow newRow, Merger merger) {
            for (var codomainRow : codomainRows)
                morphism.createMapping(newRow, codomainRow);

            // If there are multiple rows but the morphism allows at most one, they have to be merged as well. We do so by creating new merge job.
            /* Commented out because morphisms from v3 cannot be arrays
            if (!morphism.isArray() && codomainRows.size() > 1)
                merger.addMergeJob(codomainRows, morphism.cod());
            */

            // TODO Here probably should be reference jobs for the codomainRows. The newRow will reference automatically (because it is a product of merging, so a new information could have be created). The coodmainRows might need to reference as well if the following condition is met:
            // Let C \in codomainRows had a morphism to one of the original rows, O_1.
            // Another original row, O_2, had a connection to another row R.
            // Row C should reference R, but there was no connection between them prior to the merging.
            //
            // Although this situation is extremely rare, it might happen. However, in that case, a somewhat limited work is needed - we only have to look to the references that satisfy the above-mentioned condition.
        }

    }

    private class ReferenceJob {

        private final Merger merger;

        SuperIdWithValues superId;
        Set<String> technicalIds; // The rows have to have at least some values in superId but it does not have to be a valid id ...
        InstanceObject instanceObject;

        ReferenceJob(Merger merger, SuperIdWithValues superId, Set<String> technicalIds, InstanceObject instanceObject) {
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
                var builder = new SuperIdWithValues.Builder();
                builder.add(targetRow.superId);
                builder.add(reference.signatureInOther, value);

                merger.addMergeJob(builder.build(), targetRow.technicalIds, instanceObject);
            }
        }

    }

}
