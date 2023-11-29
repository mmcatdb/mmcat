package cz.matfyz.querying.algorithms;

import cz.matfyz.querying.core.MorphismColoring;
import cz.matfyz.querying.core.patterntree.KindPattern;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is responsible for the creation of query plans for a given pattern. The pattern is represented by the extracted schema category.
 */
public class QueryPlanner {

    /**
     * @param allKinds All the kinds that are used in this query pattern. Each with the part of the pattern that is mapped to it.
     * @return
     */
    public static List<Set<KindPattern>> run(List<KindPattern> allKinds) {
        return new QueryPlanner(allKinds).run();
    }
    
    private final List<KindPattern> allKinds;

    private QueryPlanner(List<KindPattern> allKinds) {
        this.allKinds = allKinds;
    }

    private List<Set<KindPattern>> run() {
        createQueryPlans();

        return plans;
    }
    
    /** The goal of the algorithm is to move some kinds from the rest list to the selected set. The selected set will then become a new plan. */
    private record StackItem(
        /** These kinds are already part of a plan. */
        Set<KindPattern> selected,
        /** These kinds are yet to be processed. */
        List<KindPattern> rest,
        MorphismColoring coloring
    ) {}

    private List<Set<KindPattern>> plans = new ArrayList<>();
    private Deque<StackItem> stack = new ArrayDeque<>();

    private void createQueryPlans() {
        final MorphismColoring initialColoring = MorphismColoring.create(allKinds);
        final List<KindPattern> initialSortedKinds = initialColoring.sortKinds(allKinds);

        stack.push(new StackItem(new TreeSet<>(), initialSortedKinds, initialColoring));

        while (!stack.isEmpty())
            processStackItem(stack.pop());
    }

    private void processStackItem(StackItem item) {
        if (item.rest.isEmpty()) {
            plans.add(item.selected);
            return;
        }

        for (final KindPattern kind : getKindsWithMinimalPrice(item.rest, item.coloring)) {
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
    private List<KindPattern> getKindsWithMinimalPrice(List<KindPattern> kindQueue, MorphismColoring coloring) {
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