package cz.matfyz.core.rsd;

import java.util.List;

public class RedundancyCandidate {

    final String type = "redundancy";
    List<RedundancyPair> redundancyPairs;
    boolean full;
    boolean selected;

    public String toString() {
        return "RedundancyCandidate{" + "type=" + type + ", redundancyPairs=" + redundancyPairs + ", full=" + full + ", selected=" + selected + '}';
    }

}
