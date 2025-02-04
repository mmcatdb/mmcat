package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents part of a query pattern that is mapped to a particular kind.
 * It's also mapped to a schema category.
 * Because each kind has a mapping which is a tree, this class also contains a root of a tree.
 */
public class PatternForKind implements Comparable<PatternForKind> {

    public final Mapping kind;
    public final PatternTree root;

    private final Map<SchemaObject, PatternTree> schemaObjectToPatternTree = new TreeMap<>();

    public PatternForKind(Mapping kind, PatternTree root) {
        this.kind = kind;
        this.root = root;

        addObject(this.root);
    }

    private void addObject(PatternTree patternTree) {
        schemaObjectToPatternTree.put(patternTree.schemaObject, patternTree);
        patternTree.children().forEach(this::addObject);
    }

    @Override public int compareTo(PatternForKind other) {
        return this.kind.compareTo(other.kind);
    }

    public PatternTree getPatternTree(SchemaObject object) {
        return schemaObjectToPatternTree.get(object);
    }

    @Override public String toString() {
        return kind.toString();
    }

}
