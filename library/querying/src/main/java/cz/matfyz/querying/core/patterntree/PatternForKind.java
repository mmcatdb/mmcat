package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.schema.SchemaObjex;

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

    private final Map<Variable, PatternTree> variableToPatternTree = new TreeMap<>();
    private final Map<SchemaObjex, PatternTree> schemaObjexToPatternTree = new TreeMap<>();

    public PatternForKind(Mapping kind, PatternTree root) {
        this.kind = kind;
        this.root = root;

        addObjex(this.root);
    }

    private void addObjex(PatternTree patternTree) {
        variableToPatternTree.put(patternTree.variable, patternTree);
        schemaObjexToPatternTree.put(patternTree.objex, patternTree);
        patternTree.children().forEach(this::addObjex);
    }

    @Override public int compareTo(PatternForKind other) {
        return this.kind.compareTo(other.kind);
    }

    public PatternTree getPatternTree(Variable variable) {
        return variableToPatternTree.get(variable);
    }

    public PatternTree getPatternTree(SchemaObjex objex) {
        return schemaObjexToPatternTree.get(objex);
    }

    @Override public String toString() {
        return kind.toString();
    }

}
