package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ReferenceCandidate(
    String type,
    @JsonIgnore Object subsetType,
    @JsonIgnore Object referredProperty,
    @JsonIgnore Object referencingProperty,
    String referred,
    String referencing,
    boolean weak,
    boolean selected
) {
    public ReferenceCandidate(
        Object subsetType,
        Object referredProperty,
        Object referencingProperty,
        String referred,
        String referencing,
        boolean weak,
        boolean selected
    ) {
        this("reference", subsetType, referredProperty, referencingProperty, referred, referencing, weak, selected);
    }

    @Override public String toString() {
        return "ReferenceCandidate{" +
               "type=" + type +
               ", referencing=" + referencing +
               ", referred=" + referred +
               ", weak=" + weak +
               ", selected=" + selected +
               '}';
    }
}
