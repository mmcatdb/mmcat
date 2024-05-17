package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ReferenceCandidate {

    final String type = "reference";
    @JsonIgnore
    transient Object subsetType;
    @JsonIgnore
    transient Object referredProperty;
    @JsonIgnore
    transient Object referencingProperty;
    String referred;
    String referencing;
    boolean weak;
    boolean selected;

    public String toString() {
        return "ReferenceCandidate{" + "type=" + type + ", referencing= " + referencing + ", referred=" + referred + ", weak=" + weak + ", selected=" + selected + '}';
    }
}

