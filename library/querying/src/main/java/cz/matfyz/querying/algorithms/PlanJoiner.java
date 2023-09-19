package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.JoinCondition;
import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaGraph;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.querytree.DatabaseNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.exception.JoinException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class is responsible for joining multiple kinds from the same pattern plan and the same database together.
 * Then it optimizes the whole query by merging different pattern plans.
 */
public class PlanJoiner {

    public static QueryNode run(Set<Kind> allKinds, SchemaCategory schema) {
        return new PlanJoiner(allKinds, schema).run();
    }

    private final Set<Kind> allKinds;
    private final SchemaCategory schema;

    private PlanJoiner(Set<Kind> allKinds, SchemaCategory schema) {
        this.allKinds = allKinds;
        this.schema = schema;
    }
    
    private QueryNode run() {
        if (allKinds.isEmpty()) {
            // TODO what now?
        }

        if (allKinds.size() == 1) {
            final var patternNode = new PatternNode(allKinds, schema, List.of());
            final var database = allKinds.stream().findFirst().get().database;

            return new DatabaseNode(patternNode, database);
        }
        
        // TODO there might be some joining needed for OPTIONAL joins?
        
        // final var newOperations = group.operations.stream().map(this::processOperation).toList();
        // TODO ignoring OPTIONAL and MINUS for now ...
        
        
        final Coloring coloring = Coloring.create(schema, allKinds);
        final List<JoinCandidate> joinCandidates = createJoinCandidates(coloring);
        final List<JoinGroup> candidateGroups = groupJoinCandidates(joinCandidates);
        final List<JoinGroup> filteredGroups = filterJoinGroups(candidateGroups);

        final var candidatesBetweenParts = new ArrayList<JoinCandidate>();
        final List<QueryPart> queryParts = createQueryParts(filteredGroups, candidatesBetweenParts);

        // return splitLeaf(queryParts, candidatesBetweenParts, newOperations, group.filters);
        // optimizeJoinPlan();

        return splitLeaf(queryParts, candidatesBetweenParts);
    }

    private List<JoinCandidate> createJoinCandidates(Coloring coloring) {
        final var output = new ArrayList<JoinCandidate>();
        for (final SchemaObject object : coloring.selectMulticolorObjects()) {
            // The set of all objects that have two or more colors.
            final var kinds = coloring.getColors(object).stream().toArray(Kind[]::new);
            // We try each pair of colors.
            for (int i = 0; i < kinds.length; i++)
                for (int j = i + 1; j < kinds.length; j++) {
                    final var candidate = tryCreateCandidate(object, kinds[i], kinds[j], coloring);
                    if (candidate != null)
                        output.add(candidate);
                }
        }
        
        return output;
    }

    private JoinCandidate tryCreateCandidate(SchemaObject object, Kind kind1, Kind kind2, Coloring coloring) {
        final var candidate1 = tryCreateIdRefCandidate(object, kind1, kind2, coloring);
        if (candidate1 != null)
            return candidate1;

        final var candidate2 = tryCreateIdRefCandidate(object, kind2, kind1, coloring);
        if (candidate2 != null)
            return candidate2;

        final Signature signature1 = findPathFromRoot(kind1, object);
        if (signature1 == null)
            return null;

        final Signature signature2 = findPathFromRoot(kind2, object);
        if (signature2 == null)
            return null;

        final var condition = new JoinCondition(signature1, signature2);

        // TODO recursion
        return new JoinCandidate(JoinType.Value, kind1, kind2, List.of(condition), 0);
    }

    /**
     * This object matches the id-ref join pattern. This means that an object (rootObject) is identified by another object (idObject). The first kind (idKind) has A as a root object and B as a normal property. The second kind (refKind) has the idObject.
     */
    private JoinCandidate tryCreateIdRefCandidate(SchemaObject idObject, Kind idKind, Kind refKind, Coloring coloring) {
        // First, check if the idObject is an identifier of the root of the idKind.
        final SchemaObject rootObject = idKind.mapping.rootObject();
        if (!rootObject.ids().isSignatures())
            return null;
        // TODO currently, we are using only the first id for joining.
        final SignatureId firstId = rootObject.ids().toSignatureIds().first();
        // TODO currently, we are accepting only signature ids with exactly one signature.
        if (firstId.signatures().size() != 1)
            return null;
        
        final BaseSignature fromSignature = firstId.signatures().first().getLast();
        final SchemaObject rootIdObject = schema.getEdge(fromSignature).to();
        if (!idObject.equals(rootIdObject))
            return null;

        final Signature toSignature = findPathFromRoot(refKind, idObject);
        if (toSignature == null)
            return null;

        final var condition = new JoinCondition(fromSignature, toSignature);
        
        // The idObject is in fact an identifier of the root of the idKind. We also know that both idKind and refKind contains the object. Therefore we can create the join candidate.
        // TODO recursion
        return new JoinCandidate(JoinType.IdRef, idKind, refKind, List.of(condition), 0);
    }

    @Nullable
    private Signature findPathFromRoot(Kind kind, SchemaObject object) {
        // TODO - constructing schema graph each time is highly ineffective.
        final var graph = new SchemaGraph(kind.mapping.category().allMorphisms());
        final List<Signature> signatures = graph.findPath(kind.mapping.rootObject(), object);

        return signatures == null
            ? null
            : Signature.concatenate(signatures);
    }

    private static record DatabasePair(Database first, Database second) implements Comparable<DatabasePair> {
        public static DatabasePair create(JoinCandidate candidate) {
            final var a = candidate.from().database;
            final var b = candidate.to().database;
            final boolean comparison = a.compareTo(b) > 0;

            return new DatabasePair(comparison ? a : b, comparison ? b : a);
        }

        @Override
        public int compareTo(DatabasePair other) {
            final int firstComparison = first.compareTo(other.first);
            return firstComparison != 0 ? firstComparison : second.compareTo(other.second);
        }

        public boolean isSameDatabase() {
            return first.equals(second);
        }
    }

    private static record JoinGroup(DatabasePair databases, List<JoinCandidate> candidates) {}

    /**
     * Groups the join candidates by the database pairs of their two kinds.
     * @param candidates
     * @return
     */
    private List<JoinGroup> groupJoinCandidates(List<JoinCandidate> candidates) {
        final var output = new TreeMap<DatabasePair, List<JoinCandidate>>();
        candidates.forEach(c -> output.computeIfAbsent(DatabasePair.create(c), p -> new ArrayList<>()));

        return output.entrySet().stream()
            .map(e -> new JoinGroup(e.getKey(), e.getValue()))
            .toList();
    }

    /**
     * If there are multiple join candidates between the same databases, we don't need them all.
     * If there are any id-ref joins, we select one of them and discard all others.
     * Otherwise, we keep all value-value joins.
     */
    private List<JoinGroup> filterJoinGroups(List<JoinGroup> groups) {
        return groups.stream().map(g -> {
            final var idRefCandidate = g.candidates.stream().filter(c -> c.type() == JoinType.IdRef).findFirst();
            return idRefCandidate.isPresent()
                ? new JoinGroup(g.databases, List.of(idRefCandidate.get()))
                : g;
        }).toList();
    }

    private List<QueryPart> createQueryParts(List<JoinGroup> groups, List<JoinCandidate> candidatesBetweenParts) {
        // First, we find all candidates between different databases and add them to the `between parts` category.
        groups.stream()
            .filter(g -> !g.databases.isSameDatabase())
            .forEach(g -> candidatesBetweenParts.addAll(g.candidates));
        
        // Then we find all candidates that join kinds from the same db and merge them.
        return groups.stream()
            .filter(g -> g.databases.isSameDatabase())
            .flatMap(g -> mergeNeighbors(g.candidates, candidatesBetweenParts).stream()).toList();
    }

    public record QueryPart(Set<Kind> kinds, List<JoinCandidate> joinCandidates) {}

    /**
     * Merges kinds from a single database to a minimal number of query parts.
     */
    private List<QueryPart> mergeNeighbors(List<JoinCandidate> candidates, List<JoinCandidate> candidatesBetweenParts) {
        // All of the candidates have to have the same database.
        final Database database = candidates.get(0).from().database;
        // If the database supports joins, we use the candidates as edges to construct graph components. Then we create one query part from each component.
        if (database.control.getQueryWrapper().isJoinSupported())
            return GraphUtils.findComponents(candidates).stream().map(c -> new QueryPart(c.nodes(), c.edges())).toList();
        
        // Othervise, we have to create custom query part for each kind.
        final var kinds = new TreeSet<Kind>();
        candidates.forEach(c -> {
            kinds.add(c.from());
            kinds.add(c.to());
        });
        // Also, all candidates will join different parts so we add them to the `between parts` category.
        candidatesBetweenParts.addAll(candidates);

        return kinds.stream().map(kind -> new QueryPart(Set.of(kind), List.of())).toList();
    }

    private static interface JoinTreeNode {
        Set<Kind> kinds();
        QueryNode toQueryNode(SchemaCategory schema);
    }

    // public interface HasKinds {
    
    //     Set<Kind> kinds();
    
    //     public static record SplitResult<T extends HasKinds>(List<T> included, List<T> rest) {}
    
    //     public static <T extends HasKinds> SplitResult<T> splitByKinds(List<T> all, Set<Kind> kinds) {
    //         final var included = new ArrayList<T>();
    //         final var rest = new ArrayList<T>();
    //         all.forEach(item -> (kinds.containsAll(item.kinds()) ? included : rest).add(item));
    
    //         return new SplitResult<>(included, rest);
    //     }
    
    // }
    
    private static record JoinTreeInner(JoinTreeNode from, JoinTreeNode to, JoinCandidate candidate, Set<Kind> kinds) implements JoinTreeNode {
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

    private static record JoinTreeLeaf(QueryPart queryPart) implements JoinTreeNode {
        public Set<Kind> kinds() {
            return queryPart.kinds;
        }

        public DatabaseNode toQueryNode(SchemaCategory schema) {
            // TODO schema
            // final var pattern = PatternNode.createFinal(kinds(), null, queryPart.joinCandidates);
            // return new GroupNode(pattern, operations, filters);
            final var patternNode = new PatternNode(queryPart.kinds, schema, queryPart.joinCandidates);
            final var database = queryPart.kinds.stream().findFirst().get().database;

            return new DatabaseNode(patternNode, database);
        }
    }

    /**
     * On the input, we get a list of query parts and a list of joins that can be used to connect kinds from different query parts (thus joining whole query parts together).
     * We construct a tree of joins (inner nodes) and query parts (leaves).
     */
    // private QueryNode splitLeaf(List<QueryPart> queryParts, List<JoinCandidate> candidates, List<OperationNode> operations, List<FilterNode> filters) {
    private QueryNode splitLeaf(List<QueryPart> queryParts, List<JoinCandidate> candidates) {
        final JoinTreeNode joinTree = computeJoinTree(queryParts, candidates);
        // The schema category is not splitted - it stays as is for all sub-patterns
        return joinTree.toQueryNode(schema);
    }

    private JoinTreeNode computeJoinTree(List<QueryPart> queryParts, List<JoinCandidate> candidates) {
        // We want to try the id-ref ones first.
        final var orderedCandidates = Stream.concat(
            candidates.stream().filter(c -> c.type() == JoinType.IdRef),
            candidates.stream().filter(c -> c.type() == JoinType.Value)
        );

        final List<JoinTreeNode> nodes = queryParts.stream().map(part -> (JoinTreeNode) new JoinTreeLeaf(part)).toList();

        orderedCandidates.forEach(c -> {
            final var fromNode = nodes.stream().filter(n -> n.kinds().contains(c.from())).findFirst().get();
            final var toNode = nodes.stream().filter(n -> n.kinds().contains(c.to())).findFirst().get();

            // Nothing to do here, the nodes are already joined.
            if (fromNode == toNode)
                return;

            nodes.remove(fromNode);
            nodes.remove(toNode);

            final var kindsUnion = new TreeSet<>(fromNode.kinds());
            kindsUnion.addAll(toNode.kinds());
            nodes.add(new JoinTreeInner(fromNode, toNode, c, kindsUnion));
        });

        // Now, there should be only one join node. If not, the query is invalid.
        if (nodes.size() != 1)
            throw JoinException.impossible();

        return nodes.get(0);
    }
    
    private void optimizeJoinPlan() {
        throw new UnsupportedOperationException();
    }

    private static class Coloring {

        private final SchemaCategory schema;
        private final Map<Key, Set<Kind>> objectColors;
        private final Map<Signature, Set<Kind>> morphismColors;

        private Coloring(SchemaCategory schema, Map<Key, Set<Kind>> objectColors, Map<Signature, Set<Kind>> morphismColors) {
            this.schema = schema;
            this.objectColors = objectColors;
            this.morphismColors = morphismColors;
        }

        public static Coloring create(SchemaCategory schema, Collection<Kind> kinds) {
            final var coloring = new Coloring(schema, new TreeMap<>(), new TreeMap<>());
            
            for (final var kind : kinds)
                coloring.colorObjectsAndMorphisms(kind, kind.mapping.accessPath());

            return coloring;
        }

        private void colorObjectsAndMorphisms(Kind kind, ComplexProperty path) {
            for (final var subpath : path.subpaths()) {
                // TODO splitting might not work there?
                subpath.signature().toBases().forEach(base -> {
                    final var edge = schema.getEdge(base);
                    morphismColors
                        .computeIfAbsent(edge.morphism().signature(), x -> new TreeSet<>())
                        .add(kind);

                    objectColors
                        .computeIfAbsent(edge.from().key(), x -> new TreeSet<>())
                        .add(kind);

                    objectColors
                        .computeIfAbsent(edge.to().key(), x -> new TreeSet<>())
                        .add(kind);
                });

                if (!(subpath instanceof ComplexProperty complexSubpath))
                    continue;

                colorObjectsAndMorphisms(kind, complexSubpath);
            }
        }

        /**
         * Select all objects that have more than one color.
         */
        public Set<SchemaObject> selectMulticolorObjects() {
            return Set.of(
                objectColors.keySet().stream().filter(key -> objectColors.get(key).size() > 1)
                    .map(schema::getObject).toArray(SchemaObject[]::new)
            );
        }

        /**
         * The same but for morphisms.
         */
        public Set<SchemaMorphism> selectMorphisms() {
            return Set.of(
                morphismColors.keySet().stream().filter(signature -> morphismColors.get(signature).size() > 1)
                    .map(schema::getMorphism).toArray(SchemaMorphism[]::new)
            );
        }

        public Set<Kind> getColors(SchemaObject object) {
            return objectColors.get(object);
        }

        public Set<Kind> getColors(SchemaMorphism morphism) {
            return morphismColors.get(morphism);
        }

    }

}