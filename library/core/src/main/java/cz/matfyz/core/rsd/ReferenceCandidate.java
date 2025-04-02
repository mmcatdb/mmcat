package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record ReferenceCandidate(
    String type,
    @JsonIgnore SubsetType subsetType,
    @JsonIgnore PropertyHeuristics referredProperty,
    @JsonIgnore PropertyHeuristics referencingProperty,
    String referred,
    String referencing,
    boolean isWeak,
    boolean isSelected
) {
    public ReferenceCandidate(
        SubsetType subsetType,
        PropertyHeuristics referredProperty,
        PropertyHeuristics referencingProperty,
        String referred,
        String referencing,
        boolean isWeak,
        boolean isSelected
    ) {
        this("reference", subsetType, referredProperty, referencingProperty, referred, referencing, isWeak, isSelected);
    }

    @Override public String toString() {
        return "ReferenceCandidate{" +
               "type=" + type +
               ", referencing=" + referencing +
               ", referred=" + referred +
               ", isWeak=" + isWeak +
               ", isSelected=" + isSelected +
               '}';
    }
}
