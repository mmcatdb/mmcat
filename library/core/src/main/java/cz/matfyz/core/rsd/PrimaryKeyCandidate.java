package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record PrimaryKeyCandidate(
    String type,
    @JsonIgnore Object property,
    String hierarchicalName,
    boolean selected
) {
    public PrimaryKeyCandidate(
        Object property,
        String hierarchicalName,
        boolean selected
    ) {
        this("primary", property, hierarchicalName, selected);
    }

    @Override
    public String toString() {
        return "PrimaryKeyCandidate{" +
                "type=" + type +
                ", hierarchicalName=" + hierarchicalName +
                ", selected=" + selected +
                '}';
    }
}
