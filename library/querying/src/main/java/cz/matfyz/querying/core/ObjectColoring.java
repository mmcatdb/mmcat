package cz.matfyz.querying.core;

import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.core.patterntree.KindPattern;
import cz.matfyz.querying.core.patterntree.PatternObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ObjectColoring {

    private final Map<SchemaObject, Set<KindPattern>> objectToColors;

    private ObjectColoring(Map<SchemaObject, Set<KindPattern>> objectToColors) {
        this.objectToColors = objectToColors;
    }

    public static ObjectColoring create(Collection<KindPattern> kinds) {
        final var coloring = new ObjectColoring(new TreeMap<>());
        
        for (final var kind : kinds)
            coloring.colorObjects(kind, kind.root);

        return coloring;
    }

    private void colorObjects(KindPattern kind, PatternObject object) {
        objectToColors
            .computeIfAbsent(object.schemaObject, x -> new TreeSet<>())
            .add(kind);

        for (final var child : object.children())
            colorObjects(kind, child);
    }

    /**
     * Select all objects that have more than one color.
     */
    public List<SchemaObject> selectMulticolorObjects() {
        return objectToColors.keySet().stream().filter(key -> objectToColors.get(key).size() > 1).toList();
    }

    public Set<KindPattern> getColors(SchemaObject object) {
        return objectToColors.get(object);
    }

}