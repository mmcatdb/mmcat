package cz.matfyz.querying.core;

import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.querying.core.patterntree.KindPattern;
import cz.matfyz.querying.core.patterntree.PatternObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MorphismColoring {

    // Set of all kinds that use given morphism.
    private final Map<BaseSignature, Set<KindPattern>> morphismToColors;
    // Set of all morphisms in given kind pattern.
    private final Map<KindPattern, Set<BaseSignature>> colorToMorphism;

    // private final Map<Key, Set<KindPattern> a;

    private MorphismColoring(Map<BaseSignature, Set<KindPattern>> colors, Map<KindPattern, Set<BaseSignature>> morphisms) {
        this.morphismToColors = colors;
        this.colorToMorphism = morphisms;
    }

    public static MorphismColoring create(Collection<KindPattern> kindPatterns) {
        final var coloring = new MorphismColoring(new TreeMap<>(), new TreeMap<>());
        
        for (final var kindPattern : kindPatterns)
            coloring.colorMorphisms(kindPattern, kindPattern.root);

        return coloring;
    }

    private void colorMorphisms(KindPattern kind, PatternObject object) {
        for (final var child : object.children()) {
            morphismToColors
                .computeIfAbsent(child.signatureFromParent(), x -> new TreeSet<>())
                .add(kind);

            colorToMorphism
                .computeIfAbsent(kind, x -> new TreeSet<>())
                .add(child.signatureFromParent());
            
            colorMorphisms(kind, child);
        }
    }

    private Map<KindPattern, Integer> kindCosts = new TreeMap<>();

    public int getKindCost(KindPattern kindPattern) {
        return kindCosts.computeIfAbsent(kindPattern, k -> computePatternCost(kindPattern.root));
    }

    /**
     * Finds the lowest number of colors assigned to any of the morphisms in the mapping.
     * If all morphisms have zero colors, 0 is returned.
     */
    private int computePatternCost(PatternObject object) {
        int min = Integer.MAX_VALUE;
        if (object.signatureFromParent() != null) {
            final var objectColors = morphismToColors.get(object.signatureFromParent());
            // TODO maybe just return 0?
            if (objectColors != null)
                min = objectColors.size();
        }

        for (final var child : object.children())
            min = Math.min(min, computePatternCost(child));

        return min == Integer.MAX_VALUE ? 0 : min;
    }

    private static record KindWithCost(KindPattern kind, int cost) {}

    /**
     * Sorts given kinds based on the coloring.
     * Also removes the zero-cost kinds because they aren't needed anymore.
     */
    public List<KindPattern> sortKinds(List<KindPattern> kindPatterns) {
        return kindPatterns.stream()
            .map(kindPattern -> new KindWithCost(kindPattern, getKindCost(kindPattern)))
            .filter(kindWithCost -> kindWithCost.cost != 0)
            .sorted((a, b) -> a.cost - b.cost)
            .map(KindWithCost::kind)
            .toList();
    }

    /**
     * Creates a new coloring (the current one stays unchanged).
     * For each morphism in the given kind, we zero the cost of the morphism in all the other kinds.
     * This basically means that we just remove all colors of the morphism.
     */
    public MorphismColoring removeKind(KindPattern kind) {
        final Set<BaseSignature> removedMorphisms = colorToMorphism.get(kind);
        final var newColors = new TreeMap<BaseSignature, Set<KindPattern>>();
        morphismToColors.forEach((signature, set) -> {
            if (!removedMorphisms.contains(signature))
                newColors.put(signature, new TreeSet<>(set));
        });

        return new MorphismColoring(newColors, colorToMorphism);
    }

}