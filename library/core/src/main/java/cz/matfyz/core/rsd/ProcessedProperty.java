package cz.matfyz.core.rsd;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

public class ProcessedProperty implements Serializable, Comparable<ProcessedProperty> {

    private Set<ProcessedProperty> children;
    private ProcessedProperty parent;
    private RecordSchemaDescription schema;
    private PropertyHeuristics heuristics;
    private String hierarchicalName;

    public static ProcessedProperty empty() {
        return null;    // TODO:
    }

    public ProcessedProperty(String hierarchicalName, RecordSchemaDescription schema) {
        this.children = new TreeSet<>();
        this.hierarchicalName = hierarchicalName;
        this.schema = schema;
    }

    public void addChild(ProcessedProperty child) {
        this.children.add(child);
    }

    @Override public int compareTo(ProcessedProperty o) {
        return this.hierarchicalName.compareTo(o.hierarchicalName);
    }

    public RecordSchemaDescription getRSD() {
        return schema;
    }

    public Set<ProcessedProperty> getChildren() {
        return children;
    }
    public void setChildren(Set<ProcessedProperty> children) {
        this.children = children;
    }
    public ProcessedProperty getParent() {
        return parent;
    }

    public void setParent(ProcessedProperty parent) {
        this.parent = parent;
    }

    public RecordSchemaDescription getSchema() {
        return schema;
    }

    public void setSchema(RecordSchemaDescription schema) {
        this.schema = schema;
    }

    public PropertyHeuristics getHeuristics() {
        return heuristics;
    }

    public void setHeuristics(PropertyHeuristics heuristics) {
        this.heuristics = heuristics;
    }

    public String getHierarchicalName() {
        return hierarchicalName;
    }

    public void setHierarchicalName(String hierarchicalName) {
        this.hierarchicalName = hierarchicalName;
    }
}
