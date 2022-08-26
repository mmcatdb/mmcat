package cz.cuni.matfyz.core.instance;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @author jachym.bartik
 */
public class Merger {

    private final Queue<MergeJob> jobs;
    private final Queue<NotifyJob> notifyJobs;

    public Merger() {
        this.jobs = new LinkedList<>();
        this.notifyJobs = new LinkedList<>();
    }

    public void add(MergeJob job) {
        this.jobs.add(job);
    }

    /**
     * Merges the row and then iterativelly merges rows from other instance objects that might be affected.
     * @param row
     * @return
     */
    public DomainRow merge(DomainRow row, InstanceObject instanceObject) {
        jobs.add(new MergeJob(row.superId, row.technicalIds, instanceObject));

        while (!jobs.isEmpty())
            mergePhase();

        return instanceObject.getActualRow(row);
    }

    // TODO make private
    public void mergePhase() {
        while (!jobs.isEmpty()) {
            var job = jobs.poll();
            job.instanceObject.merge(job.superId, job.technicalIds, this);

            // TODO make more effective:
            // - only these needed
            // - set, not a queue, so the same rows won't be repeated
            addNotifyJob(job.superId, job.technicalIds, job.instanceObject);
        }

        while (!notifyJobs.isEmpty()) {
            var job = notifyJobs.poll();
            // TODO fix get row
            var notifierRow = job.instanceObject.getActualRow(new DomainRow(job.superId, job.instanceObject));
            job.instanceObject.notifyOtherSuperIds(notifierRow, this);
        }
    }

    public void addMergeJob(Set<DomainRow> rows, InstanceObject instanceObject) {
        jobs.add(new MergeJob(rows, instanceObject));
    }

    // TODO check
    public void addMergeJob(IdWithValues superId, Set<Integer> technicalId, InstanceObject instanceObject) {
        jobs.add(new MergeJob(superId, technicalId, instanceObject));
    }

    public void addNotifyJob(IdWithValues superId, Set<Integer> technicalIds, InstanceObject instanceObject) {
        notifyJobs.add(new NotifyJob(superId, technicalIds, instanceObject));
    }

    private class MergeJob {

        IdWithValues superId;
        Set<Integer> technicalIds;
        InstanceObject instanceObject;

        public MergeJob(IdWithValues superId, Set<Integer> technicalIds, InstanceObject instanceObject) {
            this.superId = superId;
            this.technicalIds = technicalIds;
            this.instanceObject = instanceObject;
        }

        public MergeJob(Set<DomainRow> rows, InstanceObject instanceObject) {
            this.superId = InstanceObject.mergeSuperIds(rows);
            this.technicalIds = InstanceObject.mergeTechnicalIds(rows);
            this.instanceObject = instanceObject;
        }

    }

    private class NotifyJob {

        IdWithValues superId;
        Set<Integer> technicalIds; // The rows have to have at least some values in superId but it does not have to be a valid id ...
        InstanceObject instanceObject;

        public NotifyJob(IdWithValues superId, Set<Integer> technicalIds, InstanceObject instanceObject) {
            this.superId = superId;
            this.technicalIds = technicalIds;
            this.instanceObject = instanceObject;
        }

    }

}
