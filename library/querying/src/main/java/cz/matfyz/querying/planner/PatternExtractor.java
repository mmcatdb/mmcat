package cz.matfyz.querying.planner;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.PatternTree;
import cz.matfyz.querying.normalizer.NormalizedQuery.SelectionClause;
import cz.matfyz.querying.core.patterntree.PatternForKind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * From each mapping, this class extracts a pattern that is relevant for the query.
 */
public class PatternExtractor {

    public static List<PatternForKind> run(QueryContext context, List<Mapping> kinds, SelectionClause clause) {
        return new PatternExtractor(context, kinds, clause).run();
    }

    private final QueryContext context;
    private final List<Mapping> kinds;
    private final SelectionClause clause;

    private PatternExtractor(QueryContext context, List<Mapping> kinds, SelectionClause clause) {
        this.context = context;
        this.kinds = kinds;
        this.clause = clause;
    }

    private Map<Key, Variable> keyToVariable = new TreeMap<>();

    private List<PatternForKind> run() {
        /** List of all morphisms that appear directly in the pattern. They already contain only base signatures without duals. */
        final List<SchemaMorphism> patternMorphisms = new ArrayList<>();

        GraphUtils.forEachDFS(clause.variables(), tree -> {
            if (tree.edgeFromParent == null)
                // Root - just skip it.
                return;

            final var edge = context.getSchema().getEdge(tree.edgeFromParent);
            patternMorphisms.add(edge.morphism());

            keyToVariable.put(edge.from().key(), tree.parent().variable);
            keyToVariable.put(edge.to().key(), tree.variable);
        });

        createNewCategory(patternMorphisms);

        final var patterns = createPatternsForKinds();
        // At this point, we can check whether the patterns cover all morphisms from the query. But it isn't necessary, because if some morphisms aren't covered, the KindPlanner shouldn't be able to create any plan.

        return patterns;
    }

    private final Queue<SchemaMorphism> morphismQueue = new ArrayDeque<>();
    // We extract all objexes and morphisms that are reachable from the pattern plus those that are needed to identify the objexes.
    private final Set<Key> extractedObjexes = new TreeSet<>();
    private final Set<Signature> extractedMorphisms = new TreeSet<>();

    private void createNewCategory(List<SchemaMorphism> patternMorphisms) {
        morphismQueue.addAll(patternMorphisms);

        // We have to use queue because the morphisms need to add objexes which need to add their ids which consist of objexes and morphisms ... so we have to break the chain somewhere.
        while (!morphismQueue.isEmpty())
            addMorphism(morphismQueue.poll());
    }

    private void addMorphism(SchemaMorphism morphism) {
        // There are no duals in the queue on the start and we aren't adding them during the process. So this is safe.
        if (extractedMorphisms.contains(morphism.signature()))
            return;

        extractedMorphisms.add(morphism.signature());
        addObjex(morphism.dom());
        addObjex(morphism.cod());
    }

    private void addObjex(SchemaObjex objex) {
        if (extractedObjexes.contains(objex.key()))
            return;

        extractedObjexes.add(objex.key());

        if (!objex.hasSignatureId())
            return;

        objex.ids().collectAllSignatures().stream()
            .flatMap(signature -> signature.toBases().stream())
            // We don't have to worry about duals here because ids can't contain them (ids have to have cardinality at most 1).
            .forEach(base -> morphismQueue.add(context.getSchema().getMorphism(base)));
    }

    private List<PatternForKind> createPatternsForKinds() {
        return kinds.stream()
            .filter(kind -> extractedObjexes.contains(kind.rootObjex().key()))
            .map(kind -> {
                final var rootObjex = kind.rootObjex();
                // TODO really?
                // The root has to be variable.
                final var rootVariable = getOrCreateVariableForObjex(rootObjex);
                final var rootNode = PatternTree.createRoot(rootObjex, rootVariable);
                processComplexProperty(rootNode, kind.accessPath());

                return new PatternForKind(kind, rootNode);
            }).toList();
    }

    private void processComplexProperty(PatternTree node, ComplexProperty path) {
        path.subpaths().stream()
            .forEach(subpath -> {
                // TODO - is this going to work? Because it might not be possible to browse a database with composed signatures only.
                var currentNode = node;
                for (final var base : subpath.signature().toBases()) {
                    if (!extractedMorphisms.contains(base.toAbsolute()))
                        return;

                    final SchemaEdge edge = context.getSchema().getEdge(base);
                    final Variable childVariable = getOrCreateVariableForObjex(edge.to());

                    currentNode = currentNode.getOrCreateChild(edge, childVariable);
                }
                // If the subpath is an auxiliary property, the signature split leads to an empty list. Therefore, it's automatically skipped and we continue with its children.

                if (subpath instanceof ComplexProperty complex)
                    processComplexProperty(currentNode, complex);
            });
    }

    /**
     * Gets an variable for an objex. If it's missing, a new one is created.
     * The only valid reason for it to be missing is that the objex was added during the extraction because it's an identifier of some other objex.
     * The created variable is a variable only - it isn't added to the variable tree.
     * TODO This last line is sus. Investigate.
     */
    private Variable getOrCreateVariableForObjex(SchemaObjex objex) {
        final Variable foundVariable = keyToVariable.get(objex.key());
        if (foundVariable != null)
            return foundVariable;

        final var newVariable = clause.scope().variable.createGenerated();
        keyToVariable.put(objex.key(), newVariable);

        return newVariable;
    }

}
