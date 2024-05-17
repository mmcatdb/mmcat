package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PrimaryKeyCandidate {
    final String type = "primary";
    @JsonIgnore
    transient Object property;
    String hierarchicalName;
    boolean selected;

    public String toString() {
        return "PrimaryKeyCandidate{" + "type=" + type + ", hierarchicalName=" + hierarchicalName + ", selected=" + selected + '}';
    }
}
