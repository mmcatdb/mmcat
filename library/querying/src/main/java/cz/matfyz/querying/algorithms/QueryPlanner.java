package cz.matfyz.querying.algorithms;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.MorphismColoring;
import cz.matfyz.querying.core.patterntree.KindPattern;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

/**
 * This class is responsible for the creation of query plans for a given pattern. The pattern is represented by the extracted schema category.
 */
public class QueryPlanner {

    /**
     * @param schema The queried schema category.
     * @param kindPatterns All the kinds that are used in this query pattern. Each with the part of the pattern that is mapped to it.
     * @return
     */
    public static List<Set<KindPattern>> run(SchemaCategory schema, List<KindPattern> kindPatterns) {
        return new QueryPlanner(schema, kindPatterns).run();
    }
    
    private final SchemaCategory schema;
    private final List<KindPattern> kindPatterns;

    private QueryPlanner(SchemaCategory schema, List<KindPattern> kindPatterns) {
        this.schema = schema;
        this.kindPatterns = kindPatterns;
    }

    private List<Set<KindPattern>> run() {
        createQueryPlans();

        return plans;
    }
    
    private record StackItem(
        Set<KindPattern> selected,
        List<KindPattern> rest,
        MorphismColoring coloring
    ) {}

    private List<Set<KindPattern>> plans = new ArrayList<>();
    private Stack<StackItem> stack = new Stack<>();

    private void createQueryPlans() {
        final MorphismColoring initialColoring = MorphismColoring.create(kindPatterns);
        final List<KindPattern> initialSortedKinds = initialColoring.sortKinds(kindPatterns);

        stack.push(new StackItem(new TreeSet<>(), new LinkedList<>(initialSortedKinds), initialColoring));

        while (!stack.isEmpty())
            processStackItem(stack.pop());
    }

    private void processStackItem(StackItem item) {
        if (item.rest.isEmpty()) {
            plans.add(item.selected);
            return;
        }

        for (final KindPattern kind : touchFirst(item.rest, item.coloring)) {
            final List<KindPattern> restWithoutKind = item.rest.stream().filter(k -> !k.equals(kind)).toList();

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
    private List<KindPattern> touchFirst(List<KindPattern> kindQueue, MorphismColoring coloring) {
        final int lowestCost = coloring.getKindCost(kindQueue.get(0));
        final List<KindPattern> output = new ArrayList<>();

        for (final var kind : kindQueue) {
            if (coloring.getKindCost(kind) != lowestCost)
                break;

            output.add(kind);
        }

        return output;
    }

}