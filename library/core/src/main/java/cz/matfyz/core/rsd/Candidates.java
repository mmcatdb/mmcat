package cz.matfyz.core.rsd;

import java.util.ArrayList;
import java.util.List;

public class Candidates {
    List<PrimaryKeyCandidate> pkCandidates = new ArrayList<>();
    List<ReferenceCandidate> refCandidates = new ArrayList<>();
    List<RedundancyCandidate> redCandidates = new ArrayList<>();

    public List<PrimaryKeyCandidate> getPkCandidates() {
        return pkCandidates;
    }

    public void setPkCandidates(List<PrimaryKeyCandidate> pkCandidates) {
        this.pkCandidates = pkCandidates;
    }

    public List<ReferenceCandidate> getRefCandidates() {
        return refCandidates;
    }

    public void setRefCandidates(List<ReferenceCandidate> refCandidates) {
        this.refCandidates = refCandidates;
    }

    public List<RedundancyCandidate> getRedCandidates() {
        return redCandidates;
    }

    public void setRedCandidates(List<RedundancyCandidate> redCandidates) {
        this.redCandidates = redCandidates;
    }

    public boolean isPkCandidatesEmpty() {
        return pkCandidates.isEmpty();
    }

    public boolean isRefCandidatesEmpty() {
        return refCandidates.isEmpty();
    }

    public boolean isRedCandidatesEmpty() {
        return redCandidates.isEmpty();
    }
    // get the first PK candidate which will be in the candidate array and wont end with a certain string (most probably "/_id")
    public static PrimaryKeyCandidate firstPkCandidatesThatNotEndWith(Candidates candidates, String s) {
        for (PrimaryKeyCandidate candidate : candidates.pkCandidates) {
            if (!candidate.getHierarchicalName().endsWith(s)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Candidates{" + "pkCandidates=" + pkCandidates + ", refCandidates=" + refCandidates + ", redCandidates=" + redCandidates + '}';
    }
}
