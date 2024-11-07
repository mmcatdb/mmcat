package cz.matfyz.querying.algorithms;

import cz.matfyz.core.utils.printable.*;
import cz.matfyz.querying.core.MorphismColoring;
import cz.matfyz.querying.core.patterntree.PatternForKind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for creating query plans for a given pattern. The pattern is represented by the extracted schema category.
 */
public class QueryPlanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryPlanner.class);

    /**
     * @param allPatterns All the kinds that are used in this query pattern. Each with the part of the pattern that is mapped to it.
     * @return
     */
    public static List<Set<PatternForKind>> run(List<PatternForKind> allPatterns) {
        return new QueryPlanner(allPatterns).run();
    }

    private final List<PatternForKind> allPatterns;

    private QueryPlanner(List<PatternForKind> allPatterns) {
        this.allPatterns = allPatterns;
    }

    private List<Set<PatternForKind>> run() {
        createQueryPlans();

        return plans;
    }

    /**
     * The goal of the algorithm is to move some kinds from the rest list to the selected set. The selected set will then become a new plan.
     */
    private record StackItem(
        /** These kinds are already part of a plan. */
        Set<PatternForKind> selected,
        /** These kinds are yet to be processed. */
        List<PatternForKind> rest,
        MorphismColoring coloring
    ) implements Printable {
        @Override public void printTo(Printer printer) {
            printer
                .append("{").down().nextLine()
                .append("selected: ").append(selected).nextLine()
                .append("rest: ").append(rest).nextLine()
                .append("coloring: ").append(coloring).up().nextLine()
                .append("}");
        }

        @Override public String toString() {
            return Printer.print(this);
        }
    }

    private List<Set<PatternForKind>> plans = new ArrayList<>();
    private Deque<StackItem> stack = new ArrayDeque<>();

    private void createQueryPlans() {
        final MorphismColoring initialColoring = MorphismColoring.create(allPatterns);
        final List<PatternForKind> initialSortedKinds = initialColoring.sortPatterns(allPatterns);

        stack.push(new StackItem(new TreeSet<>(), initialSortedKinds, initialColoring));

        while (!stack.isEmpty())
            processStackItem(stack.pop());
    }

    private void processStackItem(StackItem item) {
        LOGGER.debug("Process item: \n{}", item);
        if (item.rest.isEmpty()) {
            plans.add(item.selected);
            LOGGER.debug("Build plan: {}", item.selected);
            return;
        }

        for (final PatternForKind pattern : getPatternsWithMinimalPrice(item.rest, item.coloring)) {
            LOGGER.debug("Kind: {}, price: {}", pattern, item.coloring.getPatternCost(pattern));
            final List<PatternForKind> restWithoutKind = item.rest.stream().filter(k -> !k.equals(pattern)).toList();

            final var coloringWithoutKind = item.coloring.removePattern(pattern);
            final var sortedRestKinds = coloringWithoutKind.sortPatterns(restWithoutKind);
            final var selectedWithKind = new TreeSet<>(item.selected);
            selectedWithKind.add(pattern);

            stack.push(new StackItem(selectedWithKind, sortedRestKinds, coloringWithoutKind));
        }
    }

    /**
     * Returns all kinds from the given queue with the minimal price.
     */
    private List<PatternForKind> getPatternsWithMinimalPrice(List<PatternForKind> patternQueue, MorphismColoring coloring) {
        final int lowestCost = coloring.getPatternCost(patternQueue.get(0));
        final List<PatternForKind> output = new ArrayList<>();

        for (final var pattern : patternQueue) {
            if (coloring.getPatternCost(pattern) != lowestCost)
                break;

            output.add(pattern);
        }

        return output;
    }

}
