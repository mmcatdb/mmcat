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

    public String getReferred() {
        return referred;
    }

    public void setReferred(String referred) {
        this.referred = referred;
    }

    public String getReferencing() {
        return referencing;
    }

    public void setReferencing(String referencing) {
        this.referencing = referencing;
    }

    public boolean isWeak() {
        return weak;
    }

    public void setWeak(boolean weak) {
        this.weak = weak;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Object getSubsetType() {
        return subsetType;
    }

    public void setSubsetType(Object subsetType) {
        this.subsetType = subsetType;
    }

    public Object getReferredProperty() {
        return referredProperty;
    }

    public void setReferredProperty(Object referredProperty) {
        this.referredProperty = referredProperty;
    }

    public Object getReferencingProperty() {
        return referencingProperty;
    }

    public void setReferencingProperty(Object referencingProperty) {
        this.referencingProperty = referencingProperty;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return "ReferenceCandidate{" + "type=" + type + ", referencing= " + referencing + ", referred=" + referred + ", weak=" + weak + ", selected=" + selected + '}';
    }
}

