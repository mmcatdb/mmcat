package cz.matfyz.core.instance;

import cz.matfyz.core.instance.InstanceObjex.Reference;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Merger {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(Merger.class);

    private final InstanceCategory instance;
    private final Queue<MergeRowsJob> jobs;
    private final Queue<ReferenceJob> referenceJobs;

    Merger(InstanceCategory instance) {
        this.instance = instance;
        this.jobs = new ArrayDeque<>();
        this.referenceJobs = new ArrayDeque<>();
    }

    private void processQueues() {
        while (!jobs.isEmpty())
            mergePhase();
    }

    private void mergePhase() {
        while (!jobs.isEmpty()) {
            final var job = jobs.poll();
            job.process();

            // TODO make more effective:
            // - only those that are needed
            // - set, not a queue, so the same rows won't be repeated
            addReferenceJob(job.values, job.technicalId, job.instanceObjex);
        }

        while (!referenceJobs.isEmpty()) {
            final var job = referenceJobs.poll();
            job.process();
        }
    }

    /**
     * Merges the row and then iterativelly merges rows from other instance objexes that might be affected.
     *
     * @param values The super id must contain at least one id.
     */
    public DomainRow merge(SuperIdValues values, InstanceObjex instanceObjex) {
        addMergeJob(values, null, instanceObjex);
        processQueues();

        return instanceObjex.getActualRow(values, null);
    }

    /**
     * @param values SuperId values of the new row we want to create.
     * @param parent Parent row to which we should connect the new row.
     * @param edge Edge from parent to the new row.
     * @return
     */
    public DomainRow merge(SuperIdValues values, DomainRow parent, SchemaEdge edge) {
        final InstanceObjex childObjex = instance.getObjex(edge.to());

        // First, we try to find the row by the superId values.
        final var currentRow = childObjex.getRow(values);
        if (currentRow != null)
            return addToRowAndConnect(currentRow, values, parent, edge, childObjex);

        // Then we try to find it by the connection.
        if (!edge.isArray()) {
            final var mapping = parent.getMappingsForEdge(edge).stream().findFirst();
            if (mapping.isPresent())
                return addToRow(mapping.get().codomainRow(), values, childObjex);
        }

        // No such row exists yet, so we have to create it. It also cannot be merged so we are not doing that.
        final var newRow = childObjex.createRow(values);
        createMappingForEdge(edge, parent, newRow);

        addReferenceJob(newRow.values, newRow.technicalId, childObjex);
        processQueues();

        return newRow;
    }

    private DomainRow addToRowAndConnect(DomainRow currentRow, SuperIdValues values, DomainRow parent, SchemaEdge edge, InstanceObjex childObjex) {
        // TODO more effective search, e.g., map.
        for (final var codomainRow : parent.getCodomainForEdge(edge))
            if (codomainRow.equals(currentRow))
                return addToRow(currentRow, values, childObjex); // The connection already exists so we just have to add to the values.

        // The connection does not exist yet, so we create it and then merge it.
        // TODO optimization - merging with the knowledge of the connection, so we would not have create it, then delete it and then create it for the new row.
        createMappingForEdge(edge, parent, currentRow);
        return addToRow(currentRow, values, childObjex);
    }

    private void createMappingForEdge(SchemaEdge edge, DomainRow domainRow, DomainRow codomainRow) {
        final InstanceMorphism morphism = instance.getMorphism(edge.morphism());
        if (edge.direction())
            morphism.createMapping(domainRow, codomainRow);
        else
            morphism.createMapping(codomainRow, domainRow);
    }

    /**
     * Add information from the superId values to the existing row.
     */
    private DomainRow addToRow(DomainRow currentRow, SuperIdValues values, InstanceObjex objex) {
        final var newValues = SuperIdValues.merge(currentRow.values, values);
        if (newValues.size() == currentRow.values.size())
            return currentRow; // The row already contains everything from the merging values.

        return merge(values, objex);
    }

    private void addMergeJob(SuperIdValues values, @Nullable Integer technicalId, InstanceObjex instanceObjex) {
        jobs.add(new MergeRowsJob(this, values, technicalId, instanceObjex));
    }

    private void addReferenceJob(SuperIdValues values, @Nullable Integer technicalId, InstanceObjex instanceObjex) {
        referenceJobs.add(new ReferenceJob(this, values, technicalId, instanceObjex));
    }

    private class MergeRowsJob {

        private final Merger merger;

        SuperIdValues values;
        @Nullable Integer technicalId;
        InstanceObjex instanceObjex;

        MergeRowsJob(Merger merger, SuperIdValues values, @Nullable Integer technicalId, InstanceObjex instanceObjex) {
            this.merger = merger;
            this.values = values;
            this.technicalId = technicalId;
            this.instanceObjex = instanceObjex;
        }

        public void process() {
            Set<DomainRow> originalRows = new TreeSet<>();

            // Iteratively get all rows that are identified by the superId values (while expanding the superId values).
            // Also get all technical ids.
            if (technicalId != null) {
                final var technicalRow = instanceObjex.getRowByTechnicalId(technicalId);
                originalRows.add(technicalRow);
                values = SuperIdValues.merge(values, technicalRow.values);
            }

            final var result = instanceObjex.findMaximalSuperIdValues(values, originalRows);
            final var maximalSuperIdValues = result.values();
            final var allTechnicalIds = mergeTechnicalIds(originalRows);

            if (originalRows.size() == 1)
                return; // No merging is required

            // Create new Row that contains the unified superId and put it to all possible ids.
            // This also deletes the old ones.
            final var firstTechnicalId = allTechnicalIds.isEmpty() ? null : allTechnicalIds.iterator().next();
            final var newRow = instanceObjex.createRow(maximalSuperIdValues, firstTechnicalId, result.foundIds());

            if (allTechnicalIds.size() > 1) {
                for (final var otherId : allTechnicalIds)
                    if (!otherId.equals(firstTechnicalId))
                        instanceObjex.removeTechnicalId(otherId);
            }

            // Get all morphisms from and to the original rows and put the new one instead of them.
            // Detect all morphisms that have maximal cardinality ONE and merge their rows. This can cause a chain reaction.
            // This steps is done by combining the rows' superId values and then calling merge.
            mergeMappings(originalRows, newRow);
        }

        private static Set<Integer> mergeTechnicalIds(Collection<DomainRow> rows) {
            final var output = new TreeSet<Integer>();
            for (final var row : rows)
                if (row.technicalId != null)
                    output.add(row.technicalId);

            return output;
        }

        private void mergeMappings(Set<DomainRow> originalRows, DomainRow newRow) {
            // First, we find all mappings that go from the old rows to other rows, remove them, and get their codomain rows, sorted by their morphisms.
            final Map<InstanceMorphism, Set<DomainRow>> codomainRowsForAllMorphisms = findAndRemoveMorphismsFromOriginalRows(originalRows);

            // We have to create only one mapping for each unique pair (newRow, rowTo), hence the rows to are stored in a set.
            for (final var entry : codomainRowsForAllMorphisms.entrySet()) {
                final var morphism = entry.getKey();
                final var codomainRows = entry.getValue();
                createNewMappingsForMorphism(morphism, codomainRows, newRow, merger);
            }
        }

        /**
         * Find and remove all mappings that go from the original rows to other rows. The rows to which the mappings pointed are returned, sorted by the morphisms of the mappings.
         * @param originalRows
         * @return
         */
        private Map<InstanceMorphism, Set<DomainRow>> findAndRemoveMorphismsFromOriginalRows(Set<DomainRow> originalRows) {
            final Map<InstanceMorphism, Set<DomainRow>> output = new TreeMap<>();

            for (final var row : originalRows) {
                for (final var entry : row.getAllMappingsFrom()) {
                    final var morphism = entry.morphism();
                    final var rowSet = output.computeIfAbsent(morphism, x -> new TreeSet<>());

                    for (var mappingRow : entry.mappings()) {
                        rowSet.add(mappingRow.codomainRow());

                        // Remove old mappings from their rows.
                        morphism.removeMapping(mappingRow);
                    }
                }
            }

            return output;
        }

        private static void createNewMappingsForMorphism(InstanceMorphism morphism, Set<DomainRow> codomainRows, DomainRow newRow, Merger merger) {
            for (final var codomainRow : codomainRows)
                morphism.createMapping(newRow, codomainRow);

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

        final SuperIdValues values;
        // The rows have to have at least some values directly (in the superId values) but they doesn't have to form a valid id.
        final @Nullable Integer technicalId;
        final InstanceObjex instanceObjex;

        ReferenceJob(Merger merger, SuperIdValues values, @Nullable Integer technicalId, InstanceObjex instanceObjex) {
            this.merger = merger;
            this.values = values;
            this.technicalId = technicalId;
            this.instanceObjex = instanceObjex;
        }

        public void process() {
            final var referencingRow = instanceObjex.getActualRow(values, technicalId);

            for (final var pair : referencingRow.getAndRemovePendingReferencePairs())
                for (final var reference : instanceObjex.getReferencesForSignature(pair.signature()))
                    sendReferences(referencingRow, reference, pair.value());
        }

        private void sendReferences(DomainRow sourceRow, Reference reference, String value) {
            final var targetRows = sourceRow.traverseThrough(reference.path());

            for (final var targetRow : targetRows) {
                if (!targetRow.hasSignature(reference.signatureInOther()))
                    continue; // The row already has the value.

                // Add value to the targetRow.
                final var builder = new SuperIdValues.Builder();
                builder.add(targetRow.values);
                builder.add(reference.signatureInOther(), value);

                merger.addMergeJob(builder.build(), targetRow.technicalId, instanceObjex);
            }
        }

    }

}
