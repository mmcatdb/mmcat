package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.datasource.Kind;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents part of a query pattern that is mapped to a particular kind.
 * It's also mapped to a schema category.
 * Because each kind has a mappings which is a tree, this class also contains a root of a tree.
 */
public class KindPattern implements Comparable<KindPattern> {

    public final Kind kind;
    public final PatternObject root;

    private final Map<SchemaObject, PatternObject> schemaObjectToPatternObject = new TreeMap<>();

    public KindPattern(Kind kind, PatternObject root) {
        this.kind = kind;
        this.root = root;

        addObject(this.root);
    }

    private void addObject(PatternObject patternObject) {
        schemaObjectToPatternObject.put(patternObject.schemaObject, patternObject);
        patternObject.children().forEach(this::addObject);
    }

    @Override public int compareTo(KindPattern other) {
        return this.kind.compareTo(other.kind);
    }

    public PatternObject getPatternObject(SchemaObject object) {
        return schemaObjectToPatternObject.get(object);
    }

    @Override public String toString() {
        return kind.toString();
    }

}
