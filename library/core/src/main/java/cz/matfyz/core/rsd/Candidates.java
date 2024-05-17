package cz.matfyz.core.rsd;

import java.util.ArrayList;
import java.util.List;

public class Candidates {
    List<PrimaryKeyCandidate> pkCandidates = new ArrayList<>();
    List<ReferenceCandidate> refCandidates = new ArrayList<>();
    List<RedundancyCandidate> redCandidates = new ArrayList<>();

    @Override
    public String toString() {
        return "Candidates{" + "pkCandidates=" + pkCandidates + ", refCandidates=" + refCandidates + ", redCandidates=" + redCandidates + '}';
    }
}
