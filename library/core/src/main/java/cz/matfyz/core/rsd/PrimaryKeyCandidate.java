package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record PrimaryKeyCandidate(
    String type,
    @JsonIgnore PropertyHeuristics property,
    String hierarchicalName,
    boolean isSelected
) {
    public PrimaryKeyCandidate(
        PropertyHeuristics property,
        String hierarchicalName,
        boolean isSelected
    ) {
        this("primary", property, hierarchicalName, isSelected);
    }

    @Override public String toString() {
        return "PrimaryKeyCandidate{" +
                "type=" + type +
                ", hierarchicalName=" + hierarchicalName +
                ", isSelected=" + isSelected +
                '}';
    }
}
