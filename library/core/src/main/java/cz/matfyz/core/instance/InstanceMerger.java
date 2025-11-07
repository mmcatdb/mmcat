package cz.matfyz.core.instance;

import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.instance.InstanceObjex.Reference;
import cz.matfyz.core.utils.IterableUtils;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InstanceMerger {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceMerger.class);

    private final InstanceCategory instance;
    private final Queue<MergeRowsJob> jobs = new ArrayDeque<>();
    private final Queue<ReferenceJob> referenceJobs = new ArrayDeque<>();

    InstanceMerger(InstanceCategory instance) {
        this.instance = instance;
    }

    /** During the process, we track the original row used for the merging so that we can return it later. */
    private DomainRow trackedRow;

    public DomainRow getTrackedRow() {
        return trackedRow;
    }

    public void addMergeJob(InstanceObjex objex, DomainRow row, SuperIdValues values) {
        this.trackedRow = row;

        // From the outside, we only need to merge one row with the values. However, internally, multi-row merges are needed.
        final var rows = new TreeSet<DomainRow>();
        rows.add(row);
        jobs.add(new MergeRowsJob(this, objex, rows, values));
    }

    public void addReferenceJob(InstanceObjex objex, DomainRow row) {
        this.trackedRow = row;

        referenceJobs.add(new ReferenceJob(this, objex, row));
    }

    public void processQueues() {
        while (!jobs.isEmpty())
            mergePhase();
    }

    private void mergePhase() {
        while (!jobs.isEmpty()) {
            final var job = jobs.poll();
            final var newOrUpdatedRow = job.process();

            // A new row was created (or the old one was updated), let's check its references.
            addReferenceJob(job.objex, newOrUpdatedRow);
        }

        while (!referenceJobs.isEmpty()) {
            final var job = referenceJobs.poll();
            job.process();
        }
    }

    private class MergeRowsJob {

        private final InstanceMerger merger;
        final InstanceObjex objex;
        /** Should be merged with the values. It's a set because there might be multiple such rows. */
        final Set<DomainRow> originalRows;
        /** Values that should be added to the original rows. */
        final SuperIdValues superId;

        MergeRowsJob(InstanceMerger merger, InstanceObjex objex, Set<DomainRow> originalRows, SuperIdValues superId) {
            this.merger = merger;
            this.objex = objex;
            this.originalRows = originalRows;
            this.superId = superId;
        }

        /**
         * Merges all rows corresponding to the given values into one domain row.
         */
        public DomainRow process() {
            final var mutator = new SuperIdValues.Mutator(superId);
            for (final var row : originalRows)
                mutator.add(row.superId);

            expandSuperIdByDomain(mutator);

            final var newValues = mutator.build();

            // TODO if the row is the same as one of the original rows, we could avoid creating a new one. But that's not critical.
            // That would require enabling the Domain row to update its superId, though.
            /*
            if (originalRows.size() == 1) {
                // A merge job was required, so we can expect there is some new information in the superId values.
                // However, there is still only one row. So we just update that row and move on.
                final var oldRow = originalRows.iterator().next();
                oldRow.superId = newValues;
                objex.setRow(oldRow);

                return oldRow;
            }
            */

            final var newRow = objex.createRow(newValues);
            // Invalidate the old rows' surrogate ids.
            objex.removeRows(originalRows);

            // Get all morphisms from and to the original rows and put the new one instead of them.
            // Detect all morphisms that have maximal cardinality ONE and merge their rows. This can cause a chain reaction.
            mergeMappings(originalRows, newRow);

            // FIXME this doesn't work - the tracked row is overwritten in the addReferenceJobs call above (and also addMergeJob calls that are caused by the reference jobs ...).
            if (originalRows.contains(trackedRow))
                // If the tracked row was one of the original rows, we have to update it to the new row.
                trackedRow = newRow;

            return newRow;
        }

        /**
         * Iteratively finds all rows that are identified by the values (while expanding the values).
         */
        public void expandSuperIdByDomain(SuperIdValues.Mutator mutator) {
            // First, we take all ids that can be created for this object, and we find those that can be filled from the given superId.
            // Then we find the rows that correspond to them and merge their superIds to the superId.
            // If it gets bigger, we try to generate other ids to find their objexes and so on ...

            final Set<SignatureId> remainingIds = IterableUtils.toSet(objex.schema.ids().signatureIds());

            while (true) {
                final int prevValuesSize = mutator.size();

                final var foundIds = new TreeSet<SignatureId>();
                for (final var id : remainingIds) {
                    if (!mutator.containsId(id))
                        continue;

                    foundIds.add(id);

                    final var row = objex.getRowsById(id).get(mutator);
                    if (row == null || originalRows.contains(row))
                        continue;

                    originalRows.add(row);
                    mutator.add(row.superId);
                }

                if (prevValuesSize == mutator.size())
                    break;

                remainingIds.removeAll(foundIds);
            }
        }

        private void mergeMappings(Set<DomainRow> originalRows, DomainRow newRow) {
            // First, we find all mappings that go from the old rows to other rows, remove them, and get their codomain rows, sorted by their morphisms.
            final Map<InstanceMorphism, Set<DomainRow>> codomainRowsByMorphisms = findAndRemoveMappingsFromOriginalRows(originalRows);

            // We have to create only one mapping for each unique pair (newRow, rowTo), hence the rows to are stored in a set.
            for (final var entry : codomainRowsByMorphisms.entrySet()) {
                final var morphism = entry.getKey();
                final var codomainRows = entry.getValue();
                for (final var codomainRow : codomainRows)
                    morphism.createMapping(newRow, codomainRow);

                // If there is a mapping to more than one row, they should be the same row. So we have to merge them.
                // We still create the mappings for them, they will be merged during the next merge job.
                if (codomainRows.size() > 1)
                    addRowsMergeJob(codomainRows);
            }

            final Map<InstanceMorphism, Set<DomainRow>> domainRowsByMorphisms = findAndRemoveMappingsToOriginalRows(originalRows);

            // The same as above but without the merging of the codomain rows.
            for (final var entry : domainRowsByMorphisms.entrySet()) {
                final var morphism = entry.getKey();
                final var domainRows = entry.getValue();

                for (final var domainRow : domainRows)
                    morphism.createMapping(domainRow, newRow);
            }

            // TODO There probably should be reference jobs for the codomainRows. The newRow will reference automatically (because it is a product of merging, so a new information could have be created). The coodmainRows might need to reference as well if the following condition is met:
            // Let C \in codomainRows had a morphism to one of the original rows, O_1.
            // Another original row, O_2, had a connection to another row R.
            // Row C should reference R, but there was no connection between them prior to the merging.
            //
            // Although this situation is extremely rare, it might happen. However, in that case, a somewhat limited work is needed - we only have to look to the references that satisfy the above-mentioned condition.
        }

        /** Find and remove all mappings that go from the original rows to other rows. The rows to which the mappings pointed are returned, sorted by the morphisms of the mappings. */
        private Map<InstanceMorphism, Set<DomainRow>> findAndRemoveMappingsFromOriginalRows(Set<DomainRow> originalRows) {
            final Map<InstanceMorphism, Set<DomainRow>> output = new TreeMap<>();

            for (final var row : originalRows) {
                for (final var entry : row.getAllMappingsFrom()) {
                    final var morphism = instance.getMorphism(entry.getKey());
                    final var rowSet = output.computeIfAbsent(morphism, x -> new TreeSet<>());

                    final var mappingRow = entry.getValue();
                    rowSet.add(mappingRow.cod());

                    // Remove old mappings from their rows.
                    morphism.removeMapping(mappingRow);
                }
            }

            return output;
        }

        /** Find and remove all mappings that go to the original rows to other rows. The rows to which the mappings pointed are returned, sorted by the morphisms of the mappings. */
        private Map<InstanceMorphism, Set<DomainRow>> findAndRemoveMappingsToOriginalRows(Set<DomainRow> originalRows) {
            final Map<InstanceMorphism, Set<DomainRow>> output = new TreeMap<>();

            for (final var row : originalRows) {
                for (final var entry : row.getAllMappingsTo()) {
                    final var morphism = instance.getMorphism(entry.getKey());
                    final var rowSet = output.computeIfAbsent(morphism, x -> new TreeSet<>());

                    // There might be multiple mappings for the same morphism, but that's completely legal for the "to" mappings.
                    for (final var mappingRow : entry.getValue()) {
                        rowSet.add(mappingRow.dom());
                        // Remove old mappings from their rows.
                        morphism.removeMapping(mappingRow);
                    }
                }
            }

            return output;
        }

        private void addRowsMergeJob(Set<DomainRow> rows) {
            merger.jobs.add(new MergeRowsJob(merger, objex, rows, SuperIdValues.empty()));
        }

    }

    private class ReferenceJob {

        private final InstanceMerger merger;
        final InstanceObjex objex;
        final DomainRow referencingRow;

        ReferenceJob(InstanceMerger merger, InstanceObjex objex, DomainRow referencingRow) {
            this.merger = merger;
            this.objex = objex;
            this.referencingRow = referencingRow;
        }

        public void process() {
            for (final var pair : referencingRow.getAndRemovePendingReferencePairs())
                for (final var reference : objex.getReferencesForSignature(pair.signature()))
                    sendReferences(referencingRow, reference, pair.value());
        }

        private void sendReferences(DomainRow sourceRow, Reference reference, String value) {
            final var targetRows = sourceRow.traverseThrough(reference.path());

            for (final var targetRow : targetRows) {
                if (!targetRow.superId.hasSignature(reference.signatureInOther()))
                    continue; // The row already has the value.

                // Add value to the targetRow.
                final var newValue = new SuperIdValues.Mutator().add(reference.signatureInOther(), value).build();

                merger.addMergeJob(objex, targetRow, newValue);
            }
        }

    }

}
