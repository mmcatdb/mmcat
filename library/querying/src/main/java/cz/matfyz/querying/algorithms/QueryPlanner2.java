package cz.matfyz.querying.algorithms;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Kind;
import cz.matfyz.core.mapping.Kind.KindBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.KindDefinition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class is responsible for the creation of query plans for a given pattern. The pattern is represented by the extracted schema category.
 */
public class QueryPlanner2 {

    private final SchemaCategory schema;
    private final List<Kind> allKinds;

    public QueryPlanner2(SchemaCategory schema, List<KindDefinition> kinds) {
        this.schema = schema;
        final var kindBuilder = new KindBuilder();
        this.allKinds = kinds.stream().map(k -> kindBuilder.next(k.mapping, k.databaseId)).toList();
    }

    public List<Set<Kind>> run() {
        createQueryPlans();

        return plans;
    }
    
    private record StackItem(
        Set<Kind> selected,
        List<Kind> rest,
        Coloring coloring
    ) {}

    private List<Set<Kind>> plans = new ArrayList<>();
    private Stack<StackItem> stack = new Stack<>();

    private void createQueryPlans() {
        final Coloring initialColoring = Coloring.create(schema, allKinds);
        final List<Kind> initialSortedKinds = initialColoring.sortKinds(allKinds);

        stack.push(new StackItem(new TreeSet<>(), new LinkedList<>(initialSortedKinds), initialColoring));

        while (!stack.isEmpty())
            processStackItem(stack.pop());
    }

    private void processStackItem(StackItem item) {
        if (item.rest.isEmpty()) {
            plans.add(item.selected);
            return;
        }

        for (final Kind kind : touchFirst(item.rest, item.coloring)) {
            final List<Kind> restWithoutKind = item.rest.stream().filter(k -> !k.equals(kind)).toList();

            final var coloringWithoutKind = item.coloring.removeKind(kind);
            final var sortedRestKinds = coloringWithoutKind.sortKinds(restWithoutKind);
            final var selectedWithKind = new TreeSet<>(item.selected);
            selectedWithKind.add(kind);

            stack.push(new StackItem(selectedWithKind, sortedRestKinds, coloringWithoutKind));
        }
    }

    /**
     * Returns all kinds from the given queue with the minimal price.
     */
    private List<Kind> touchFirst(List<Kind> kindQueue, Coloring coloring) {
        final int lowestCost = coloring.getKindCost(kindQueue.get(0));
        final List<Kind> output = new ArrayList<>();

        for (final var kind : kindQueue) {
            if (coloring.getKindCost(kind) != lowestCost)
                break;

            output.add(kind);
        }

        return output;
    }

    private static Set<Signature> getAllMorphismsInKind(Kind kind) {
        final Set<Signature> output = new TreeSet<>();
        addAllMorphismsInPath(kind.mapping.accessPath(), output);

        return output;
    }

    private static void addAllMorphismsInPath(AccessPath path, Set<Signature> output) {
        output.add(path.signature());
        if (path instanceof ComplexProperty complex)
            complex.subpaths().forEach(subpath -> addAllMorphismsInPath(subpath, output));
    }

    private static class Coloring {

        private final SchemaCategory schema;
        private final Map<Signature, Set<Kind>> colors;

        private Coloring(SchemaCategory schema, Map<Signature, Set<Kind>> colors) {
            this.schema = schema;
            this.colors = colors;
        }

        public static Coloring create(SchemaCategory schema, List<Kind> kinds) {
            final var coloring = new Coloring(schema, new TreeMap<>());
            
            for (final var kind : kinds)
                coloring.colorMorphisms(kind, kind.mapping.accessPath());

            return coloring;
        }

        private void colorMorphisms(Kind kind, ComplexProperty path) {
            for (final var subpath : path.subpaths()) {
                // TODO splitting might not work there?
                subpath.signature().toBases().forEach(base -> {
                    final var edge = schema.getEdge(base);
                    colors
                        .computeIfAbsent(edge.morphism().signature(), x -> new TreeSet<>())
                        .add(kind);
                });

                if (!(subpath instanceof ComplexProperty complexSubpath))
                    continue;

                colorMorphisms(kind, complexSubpath);
            }
        }

        private Map<Kind, Integer> kindCosts = new TreeMap<>();

        public int getKindCost(Kind kind) {
            return kindCosts.computeIfAbsent(kind, k -> computePathCost(k.mapping.accessPath()));
        }

        private int computePathCost(AccessPath path) {
            int min = colors.get(path.signature()).size();

            if (path instanceof ComplexProperty complex)
                for (final var subpath : complex.subpaths())
                    min = Math.min(min, computePathCost(subpath));

            return min;
        }

        private static record KindWithCost(Kind kind, int cost) {}

        /**
         * Sorts given kinds based on the coloring.
         * Also removes the zero-cost kinds because they aren't needed anymore.
         */
        public List<Kind> sortKinds(List<Kind> kinds) {
            return kinds.stream()
                .map(kind -> new KindWithCost(kind, getKindCost(kind)))
                .filter(kindWithCost -> kindWithCost.cost > 0)
                .sorted((a, b) -> a.cost - b.cost)
                .map(KindWithCost::kind)
                .toList();
        }

        /**
         * Creates a new coloring (the current one stays unchanged).
         * For each morphism in the given kind, we zero the cost of the morphism in all the other kinds.
         * This basically means that we just remove all colors of the morphism.
         */
        public Coloring removeKind(Kind kind) {
            final Set<Signature> removedMorphisms = getAllMorphismsInKind(kind);
            final var newColors = new TreeMap<Signature, Set<Kind>>();
            colors.forEach((signature, set) -> {
                if (!removedMorphisms.contains(signature))
                    newColors.put(signature, new TreeSet<>(set));
            });

            return new Coloring(schema, newColors);
        }

    }

}