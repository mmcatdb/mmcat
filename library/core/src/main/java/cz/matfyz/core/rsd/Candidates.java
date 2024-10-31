package cz.matfyz.core.rsd;

import java.util.ArrayList;
import java.util.List;

public class Candidates {
    public final List<PrimaryKeyCandidate> pkCandidates = new ArrayList<>();
    public final List<ReferenceCandidate> refCandidates = new ArrayList<>();
    //public final List<RedundancyCandidate> redCandidates = new ArrayList<>();

    @Override public String toString() {
        return "Candidates{" + "pkCandidates=" + pkCandidates + ", refCandidates=" + refCandidates + ", redCandidates=" + '}';
    }
}
