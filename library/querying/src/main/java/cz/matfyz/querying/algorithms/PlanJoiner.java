package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.JoinCondition;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.core.utils.GraphUtils.Component;
import cz.matfyz.querying.core.ObjectColoring;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.exception.JoiningException;
import cz.matfyz.querying.parsing.Term;
import cz.matfyz.querying.parsing.TermTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class is responsible for joining multiple kinds from the same pattern plan. The kinds might be from different datasources.
 */
public class PlanJoiner {

    public static QueryNode run(QueryContext context, Set<PatternForKind> allPatterns, TermTree<BaseSignature> termTree) {
        return new PlanJoiner(context, allPatterns, termTree).run();
    }

    private final QueryContext context;
    private final Set<PatternForKind> allPatterns;
    private final TermTree<BaseSignature> termTree;

    private PlanJoiner(QueryContext context, Set<PatternForKind> allPatterns, TermTree<BaseSignature> termTree) {
        this.allPatterns = allPatterns;
        this.context = context;
        this.termTree = termTree;
    }

    private QueryNode run() {
        if (allPatterns.isEmpty())
            throw JoiningException.noKinds();

        if (allPatterns.size() == 1) {
            // If there is only one pattern, there is nothing to join.
            final var onlyPattern = allPatterns.stream().findFirst().get(); // NOSONAR
            final var patternNode = new PatternNode(allPatterns, context.getSchema(), List.of(), onlyPattern.root.term);
            final var datasource = onlyPattern.kind.datasource();

            return new DatasourceNode(patternNode, datasource);
        }

        // TODO there might be some joining needed for OPTIONAL joins?

        final ObjectColoring coloring = ObjectColoring.create(allPatterns);
        // First, we find all possible join candidates.
        final List<JoinCandidate> joinCandidates = createJoinCandidates(coloring);
        // Then we group them by their datasource pairs. Remind you, both datasources in the pair can be the same.
        final List<JoinGroup> candidateGroups = groupJoinCandidates(joinCandidates);
        // Remove the candidates that don't make sence or aren't necessary.
        final List<JoinGroup> filteredGroups = filterJoinCandidates(candidateGroups);

        final List<JoinCandidate> candidatesBetweenParts = new ArrayList<>();
        final List<QueryPart> queryParts = createQueryParts(filteredGroups, candidatesBetweenParts);

        // return splitLeaf(queryParts, candidatesBetweenParts, newOperations, group.filters);
        // optimizeJoinPlan();

        return splitLeaf(queryParts, candidatesBetweenParts);
    }

    /**
     * Finds all possible join candidates between the kinds in the coloring. The datasources of the kinds are not considered here.
     */
    private List<JoinCandidate> createJoinCandidates(ObjectColoring coloring) {
        final var output = new ArrayList<JoinCandidate>();
        for (final SchemaObject object : coloring.selectMulticolorObjects()) {
            // The set of all objects that have two or more colors.
            final var patterns = coloring.getColors(object).stream().toArray(PatternForKind[]::new);
            // We try each pair of colors.
            for (int i = 0; i < patterns.length; i++)
                for (int j = i + 1; j < patterns.length; j++) {
                    final var candidate = tryCreateCandidate(object, patterns[i], patterns[j], coloring);
                    if (candidate != null)
                        output.add(candidate);
                }
        }

        return output;
    }

    private JoinCandidate tryCreateCandidate(SchemaObject object, PatternForKind pattern1, PatternForKind pattern2, ObjectColoring coloring) {
        final var candidate1 = tryCreateIdRefCandidate(object, pattern1, pattern2, coloring);
        if (candidate1 != null)
            return candidate1;

        final var candidate2 = tryCreateIdRefCandidate(object, pattern2, pattern1, coloring);
        if (candidate2 != null)
            return candidate2;

        final Signature signature1 = findPathFromRoot(pattern1, object);
        if (signature1 == null)
            return null;

        final Signature signature2 = findPathFromRoot(pattern2, object);
        if (signature2 == null)
            return null;

        final var condition = new JoinCondition(signature1, signature2);

        // TODO recursion, isOptional
        return new JoinCandidate(JoinType.Value, pattern1, pattern2, List.of(condition), 0, false);
    }

    /**
     * This function matches the id-ref join pattern. This means that an object (rootObject) is identified by another object (idObject). The first pattern (idPattern) has rootObject as a root object and idObject as a normal property. The second pattern (refPatterns) has the idObject as a normal property.
     */
    private JoinCandidate tryCreateIdRefCandidate(SchemaObject idObject, PatternForKind idPattern, PatternForKind refPattern, ObjectColoring coloring) {
        // First, check if the idObject is an identifier of the root of the idKind.
        final SchemaObject rootObject = idPattern.root.schemaObject;
        if (!rootObject.ids().isSignatures())
            return null;
        // TODO currently, we are using only the first id for joining.
        final SignatureId firstId = rootObject.ids().toSignatureIds().first();
        // TODO currently, we are accepting only signature ids with exactly one signature.
        if (firstId.signatures().size() != 1)
            return null;

        final BaseSignature fromSignature = firstId.signatures().first().getLast();
        final SchemaObject rootIdObject = context.getSchema().getEdge(fromSignature).to();
        if (!idObject.equals(rootIdObject))
            return null;

        final Signature toSignature = findPathFromRoot(refPattern, idObject);
        if (toSignature == null)
            return null;

        final var condition = new JoinCondition(fromSignature, toSignature);

        // The idObject is in fact an identifier of the root of the idPattern. We also know that both idPattern and refPattern contains the object. Therefore we can create the join candidate.
        // TODO recursion, isOptional
        return new JoinCandidate(JoinType.IdRef, idPattern, refPattern, List.of(condition), 0, false);
    }

    @Nullable
    private Signature findPathFromRoot(PatternForKind pattern, SchemaObject object) {
        final var patternObject = pattern.getPatternObject(object);
        return patternObject != null
            ? patternObject.computePathFromRoot()
            : null;
    }

    /** A pair of datasources. Both can be the same datasource! */
    private record DatasourcePair(Datasource first, Datasource second) implements Comparable<DatasourcePair> {
        public static DatasourcePair create(JoinCandidate candidate) {
            final var a = candidate.from().kind.datasource();
            final var b = candidate.to().kind.datasource();
            final boolean comparison = a.compareTo(b) > 0;

            return new DatasourcePair(comparison ? a : b, comparison ? b : a);
        }

        @Override public int compareTo(DatasourcePair other) {
            final int firstComparison = first.compareTo(other.first);
            return firstComparison != 0 ? firstComparison : second.compareTo(other.second);
        }

        public boolean isSameDatasource() {
            return first.equals(second);
        }
    }

    /** All candidates that have the same datasource pair. */
    private record JoinGroup(DatasourcePair datasources, List<JoinCandidate> candidates) {}

    /** Groups the join candidates by their datasource pair. */
    private List<JoinGroup> groupJoinCandidates(List<JoinCandidate> candidates) {
        final var output = new TreeMap<DatasourcePair, List<JoinCandidate>>();
        candidates.forEach(c -> output
            .computeIfAbsent(DatasourcePair.create(c), p -> new ArrayList<>())
            .add(c)
        );

        return output.entrySet().stream()
            .map(e -> new JoinGroup(e.getKey(), e.getValue()))
            .toList();
    }

    /**
     * If there are multiple join candidates between the same datasources, we don't need them all.
     * If there are any id-ref joins, we select one of them and discard all others.
     * Otherwise, we keep all value-value joins.
     */
    private List<JoinGroup> filterJoinCandidates(List<JoinGroup> groups) {
        return groups.stream().map(g -> {
            final var idRefCandidate = g.candidates.stream().filter(c -> c.type() == JoinType.IdRef).findFirst();
            return idRefCandidate.isPresent()
                ? new JoinGroup(g.datasources, List.of(idRefCandidate.get()))
                : g;
        }).toList();
    }

    private List<QueryPart> createQueryParts(List<JoinGroup> groups, List<JoinCandidate> candidatesBetweenParts) {
        final List<QueryPart> output = new ArrayList<>();

        // First, we merge all patterns from a same datasource to a minimal number of query parts.
        groups.stream()
            .filter(g -> g.datasources.isSameDatasource())
            .forEach(g -> {
                final var parts = mergeSameDatasourceCandidates(g.candidates, candidatesBetweenParts);
                output.addAll(parts);
            });

        // These pattern are already covered by the query parts.
        final Set<PatternForKind> coveredPatterns = new TreeSet<>();
        output.forEach(p -> coveredPatterns.addAll(p.patterns));

        // Now we add the candidates between different datasources to the betweenParts output.
        groups.stream()
            .filter(g -> !g.datasources.isSameDatasource())
            .forEach(g -> candidatesBetweenParts.addAll(g.candidates));

        // Lastly, we create a single-kind query part for all patterns that are not covered by the previously created query parts. Let's hope they are covered by the joins.
        allPatterns.stream()
            .filter(k -> !coveredPatterns.contains(k))
            .forEach(k -> {
                final var part = new QueryPart(Set.of(k), List.of(), k.root.term);
                output.add(part);
            });

        return output;
    }

    /** A query part is a part of query that can be executed at once in a single datasource. */
    private record QueryPart(Set<PatternForKind> patterns, List<JoinCandidate> joinCandidates, Term rootTerm) {}

    /** Merges patterns from a single datasource to a minimal number of query parts. */
    private List<QueryPart> mergeSameDatasourceCandidates(List<JoinCandidate> candidates, List<JoinCandidate> candidatesBetweenParts) {
        if (candidates.isEmpty())
            // TODO error?
            return List.of();

        // All of the candidates have to have the same datasource.
        final Datasource datasource = candidates.get(0).from().kind.datasource();
        // If the datasource supports joins, we use the candidates as edges to construct graph components. Then we create one query part from each component.
        if (context.getProvider().getControlWrapper(datasource).getQueryWrapper().isJoinSupported())
            return GraphUtils.findComponents(candidates).stream().map(this::createQueryPart).toList();

        // Othervise, we have to create custom query part for each pattern.
        final var patterns = new TreeSet<PatternForKind>();
        candidates.forEach(c -> {
            patterns.add(c.from());
            patterns.add(c.to());
        });
        // Also, all candidates will join different parts so we add them to the `between parts` category.
        candidatesBetweenParts.addAll(candidates);

        return patterns.stream().map(pattern -> new QueryPart(Set.of(pattern), List.of(), pattern.root.term)).toList();
    }

    private QueryPart createQueryPart(Component<PatternForKind, JoinCandidate> component) {
        final Set<PatternForKind> patterns = component.nodes();
        final List<JoinCandidate> joinCandidates = component.edges();

        final var rootPattern = GraphUtils.findRoots(component);
        if (rootPattern.size() != 1)
            throw new UnsupportedOperationException("Multiple root patterns in join");

        // This algorithm is based on the idea that the root term of the query part should be a common subroot to all terms in all patterns in the query part.
        // We only have to consider root terms of all patterns.
        // Of course, the root term has to be original. Therefore, we have to continue through it's parentes until we find such.
        final var rootTermTrees = patterns.stream()
            .map(k -> k.root.term)
            .map(term -> GraphUtils.findBFS(termTree, t -> t.term.equals(term)))
            .toList();

        var partRoot = GraphUtils.findSubroot(termTree, rootTermTrees);
        while (!partRoot.term.isOriginal())
            partRoot = partRoot.parent();

        return new QueryPart(patterns, joinCandidates, partRoot.term);
    }

    private interface JoinTreeNode {
        Set<PatternForKind> patterns();
        QueryNode toQueryNode(SchemaCategory schema);
    }

    // public interface HasKinds {

    //     Set<Kind> kinds();

    //     public record SplitResult<T extends HasKinds>(List<T> included, List<T> rest) {}

    //     public static <T extends HasKinds> SplitResult<T> splitByKinds(List<T> all, Set<Kind> kinds) {
    //         final var included = new ArrayList<T>();
    //         final var rest = new ArrayList<T>();
    //         all.forEach(item -> (kinds.containsAll(item.kinds()) ? included : rest).add(item));

    //         return new SplitResult<>(included, rest);
    //     }

    // }

    private record JoinTreeInner(JoinTreeNode from, JoinTreeNode to, JoinCandidate candidate, Set<PatternForKind> patterns) implements JoinTreeNode {
        public JoinNode toQueryNode(SchemaCategory schema) {
            // First, we try to move operations and filters down the tree.
            // final var fromOperations = HasKinds.splitByKinds(operations, from.kinds());
            // final var fromFilters = HasKinds.splitByKinds(filters, from.kinds());

            // Then we try to do the same with the other branch of the tree.
            // final var toOperations = HasKinds.splitByKinds(fromOperations.rest(), to.kinds());
            // final var toFilters = HasKinds.splitByKinds(fromFilters.rest(), to.kinds());
            // We can construct the joined group.
            // final var toGroup = to().toQueryNode();


            // Finally, we add the joined group as a join operation to the first group.
            // final var join = new JoinNode(toGroup, candidate);
            // final var newFromOperations = fromOperations.included();
            // newFromOperations.add(join);

            return new JoinNode(from().toQueryNode(schema), to().toQueryNode(schema), candidate);
        }
    }

    private record JoinTreeLeaf(QueryPart queryPart) implements JoinTreeNode {
        public Set<PatternForKind> patterns() {
            return queryPart.patterns;
        }

        public DatasourceNode toQueryNode(SchemaCategory schema) {
            // TODO schema
            // final var pattern = PatternNode.createFinal(kinds(), null, queryPart.joinCandidates);
            // return new GroupNode(pattern, operations, filters);
            final var patternNode = new PatternNode(queryPart.patterns, schema, queryPart.joinCandidates, queryPart.rootTerm);
            final var datasource = queryPart.patterns.stream().findFirst().get().kind.datasource();

            return new DatasourceNode(patternNode, datasource);
        }
    }

    /**
     * On the input, we have a list of query parts and a list of joins that can be used to connect kinds from different query parts (thus joining whole query parts together).
     * We construct a tree of joins (inner nodes) and query parts (leaves).
     */
    // private QueryNode splitLeaf(List<QueryPart> queryParts, List<JoinCandidate> candidates, List<OperationNode> operations, List<FilterNode> filters) {
    private QueryNode splitLeaf(List<QueryPart> queryParts, List<JoinCandidate> candidates) {
        final JoinTreeNode joinTree = computeJoinTree(queryParts, candidates);
        // The schema category is not splitted - it stays as is for all sub-patterns
        return joinTree.toQueryNode(context.getSchema());
    }

    private JoinTreeNode computeJoinTree(List<QueryPart> queryParts, List<JoinCandidate> candidates) {
        // We want to try the id-ref ones first.
        final var orderedCandidates = Stream.concat(
            candidates.stream().filter(c -> c.type() == JoinType.IdRef),
            candidates.stream().filter(c -> c.type() == JoinType.Value)
        );

        final List<JoinTreeNode> nodes = queryParts.stream()
            .map(part -> (JoinTreeNode) new JoinTreeLeaf(part))
            .collect(Collectors.toCollection(ArrayList::new));

        orderedCandidates.forEach(c -> {
            final var fromNode = nodes.stream().filter(n -> n.patterns().contains(c.from())).findFirst().get();
            final var toNode = nodes.stream().filter(n -> n.patterns().contains(c.to())).findFirst().get();

            // Nothing to do here, the nodes are already joined.
            if (fromNode == toNode)
                return;

            nodes.remove(fromNode);
            nodes.remove(toNode);

            final var kindsUnion = new TreeSet<>(fromNode.patterns());
            kindsUnion.addAll(toNode.patterns());
            nodes.add(new JoinTreeInner(fromNode, toNode, c, kindsUnion));
        });

        // Now, there should be only one join node. If not, the query is invalid.
        if (nodes.size() != 1)
            throw JoiningException.impossible();

        return nodes.get(0);
    }

    private void optimizeJoinPlan() {
        throw new UnsupportedOperationException("PlanJoiner.optimizeJoinPlan not implemented");
    }

}
