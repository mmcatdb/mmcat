package cz.matfyz.querying.core;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.utils.printable.*;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.patterntree.PatternObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MorphismColoring implements Printable {

    /** Set of all kinds that use given morphism (with their respective patterns). */
    private final Map<BaseSignature, Set<PatternForKind>> morphismToColors;
    /** Set of all morphisms in given kind pattern. */
    private final Map<PatternForKind, Set<BaseSignature>> colorToMorphisms;

    private MorphismColoring(Map<BaseSignature, Set<PatternForKind>> morphismToColors, Map<PatternForKind, Set<BaseSignature>> colorToMorphisms) {
        this.morphismToColors = morphismToColors;
        this.colorToMorphisms = colorToMorphisms;
    }

    public static MorphismColoring create(Collection<PatternForKind> patterns) {
        final var coloring = new MorphismColoring(new TreeMap<>(), new TreeMap<>());

        for (final var pattern : patterns)
            coloring.colorMorphisms(pattern, pattern.root);

        return coloring;
    }

    private void colorMorphisms(PatternForKind pattern, PatternObject object) {
        for (final var child : object.children()) {
            morphismToColors
                .computeIfAbsent(child.signatureFromParent(), x -> new TreeSet<>())
                .add(pattern);

            colorToMorphisms
                .computeIfAbsent(pattern, x -> new TreeSet<>())
                .add(child.signatureFromParent());

            colorMorphisms(pattern, child);
        }
    }

    private Map<PatternForKind, Integer> patternCosts = new TreeMap<>();

    public int getPatternCost(PatternForKind pattern) {
        return patternCosts.computeIfAbsent(pattern, k -> computePatternCost(pattern.root));
    }

    /**
     * Finds the lowest number of colors assigned to any of the morphisms (with at least one color) in the mapping.
     * If all morphisms have zero colors, 0 is returned.
     */
    private int computePatternCost(PatternObject object) {
        final int min = computePatternCostRecursive(object);

        return min == Integer.MAX_VALUE ? 0 : min;
    }

    private int computePatternCostRecursive(PatternObject object) {
        int min = Integer.MAX_VALUE;
        if (object.signatureFromParent() != null) {
            final var objectColors = morphismToColors.get(object.signatureFromParent());
            if (objectColors != null)
                min = objectColors.size();
        }

        for (final var child : object.children())
            min = Math.min(min, computePatternCostRecursive(child));

        return min;
    }

    private record PatternWithCost(PatternForKind pattern, int cost) {}

    /**
     * Sorts given patterns based on the coloring.
     * Also removes the zero-cost patterns because they aren't needed anymore.
     */
    public List<PatternForKind> sortPatterns(List<PatternForKind> patterns) {
        return patterns.stream()
            .map(pattern -> new PatternWithCost(pattern, getPatternCost(pattern)))
            .filter(patternWithCost -> patternWithCost.cost != 0)
            .sorted((a, b) -> a.cost - b.cost)
            .map(PatternWithCost::pattern)
            .toList();
    }

    /**
     * Creates a new coloring (the current one stays unchanged).
     * For each morphism in the given pattern, we zero the cost of the morphism in all the other patterns.
     * This basically means that we just remove all colors of the morphism.
     */
    public MorphismColoring removePattern(PatternForKind pattern) {
        final Set<BaseSignature> removedMorphisms = colorToMorphisms.get(pattern);
        final var newColors = new TreeMap<BaseSignature, Set<PatternForKind>>();
        morphismToColors.forEach((signature, set) -> {
            if (!removedMorphisms.contains(signature))
                newColors.put(signature, new TreeSet<>(set));
        });

        final var newMorphisms = new TreeMap<>(colorToMorphisms);
        newMorphisms.remove(pattern);

        return new MorphismColoring(newColors, newMorphisms);
    }

    @Override public void printTo(Printer printer) {
        printer.append("{").down().nextLine();

        for (final var entry : morphismToColors.entrySet())
            printer.append(entry.getKey()).append(": ").append(entry.getValue()).append(",").nextLine();

        printer.remove().up().nextLine().append("}");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

}
