package cz.matfyz.querying.core;

import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternTree;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ObjexColoring {

    private final Map<SchemaObjex, Set<PatternForKind>> objectToColors;

    private ObjexColoring(Map<SchemaObjex, Set<PatternForKind>> objectToColors) {
        this.objectToColors = objectToColors;
    }

    public static ObjexColoring create(Collection<PatternForKind> kinds) {
        final var coloring = new ObjexColoring(new TreeMap<>());

        for (final var kind : kinds)
            coloring.colorObjexes(kind, kind.root);

        return coloring;
    }

    private void colorObjexes(PatternForKind kind, PatternTree object) {
        objectToColors
            .computeIfAbsent(object.objex, x -> new TreeSet<>())
            .add(kind);

        for (final var child : object.children())
            colorObjexes(kind, child);
    }

    /**
     * Select all objexes that have more than one color.
     */
    public List<SchemaObjex> selectMulticolorObjexes() {
        return objectToColors.keySet().stream().filter(key -> objectToColors.get(key).size() > 1).toList();
    }

    public Set<PatternForKind> getColors(SchemaObjex object) {
        return objectToColors.get(object);
    }

}
