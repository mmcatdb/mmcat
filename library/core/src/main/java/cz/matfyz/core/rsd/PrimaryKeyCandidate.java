package cz.matfyz.core.rsd;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PrimaryKeyCandidate {
    final String type = "primary";
    @JsonIgnore
    transient Object property;
    String hierarchicalName;
    boolean selected;

    public String getHierarchicalName() {
        return hierarchicalName;
    }

    public void setHierarchicalName(String hierarchicalName) {
        this.hierarchicalName = hierarchicalName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Object getProperty() {
        return property;
    }

    public void setProperty(Object property) {
        this.property = property;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return "PrimaryKeyCandidate{" + "type=" + type + ", hierarchicalName=" + hierarchicalName + ", selected=" + selected + '}';
    }
}
