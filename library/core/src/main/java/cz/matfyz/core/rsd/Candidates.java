package cz.matfyz.core.rsd;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Candidates {
    public final List<PrimaryKeyCandidate> pkCandidates = new ArrayList<>();
    public final List<ReferenceCandidate> refCandidates = new ArrayList<>();
    public final List<RedundancyCandidate> redCandidates = new ArrayList<>();

    // get the first PK candidate which will be in the candidate array and wont end with a certain string (most probably "/_id")
    public static PrimaryKeyCandidate firstPkCandidatesThatNotEndWith(Candidates candidates, String s) {
        for (PrimaryKeyCandidate candidate : candidates.pkCandidates) {
            if (!candidate.hierarchicalName().endsWith(s)) {
                return candidate;
            }
        }
        //throw new NoSuchElementException("No primary key candidate that does not end with: " + s);
        return null; // returning null, when there is no such candidate
    }

    @Override
    public String toString() {
        return "Candidates{" + "pkCandidates=" + pkCandidates + ", refCandidates=" + refCandidates + ", redCandidates=" + redCandidates + '}';
    }
}
