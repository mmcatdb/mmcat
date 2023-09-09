package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.database.Database;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.Clause;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.QueryPart2;
import cz.matfyz.querying.core.JoinCandidate.JoinType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class is responsible for joining multiple kinds from the same pattern plan and the same database together.
 * Then it optimizes the whole query by merging different pattern plans.
 */
public class PlanJoiner {

    private final Clause rootClause;

    public PlanJoiner(Clause rootClause) {
        this.rootClause = rootClause;
    }

    public void run() {
        processClause(rootClause);
        optimizeJoinPlan();
    }

    private void processClause(Clause clause) {
        processPattern(clause);
        clause.nestedClauses.stream().forEach(nestedClause -> processClause(nestedClause));
    }

    private void processPattern(Clause clause) {
        if (clause.patternPlan.size() == 1) {
            final Kind kind = clause.patternPlan.stream().findFirst().get();
            clause.parts = List.of(QueryPart2.create(kind));
            return;
        }

        final Coloring coloring = Coloring.create(clause.schema, clause.patternPlan);
        final List<JoinCandidate> joinCandidates = createJoinCandidates(coloring, clause);
        final List<JoinGroup> candidateGroups = groupJoinCandidates(joinCandidates);
        final List<JoinGroup> filteredGroups = filterJoinGroups(candidateGroups);

        final var candidatesBetweenParts = new ArrayList<JoinCandidate>();
        final List<QueryPart2> queryParts = createQueryParts(filteredGroups, candidatesBetweenParts);



        // splitLeaf(clause, joinCandidates);
    }

    private List<JoinCandidate> createJoinCandidates(Coloring coloring, Clause clause) {
        final var output = new ArrayList<JoinCandidate>();
        for (final SchemaObject object : coloring.selectMulticolorObjects()) {
            // The set of all objects that have two or more colors.
            final var kinds = coloring.getColors(object).stream().toArray(Kind[]::new);
            // We try each pair of colors.
            for (int i = 0; i < kinds.length; i++)
                for (int j = i + 1; j < kinds.length; j++) {
                    final var candidate = tryCreateCandidate(object, kinds[i], kinds[j], clause, coloring);
                    if (candidate != null)
                        output.add(candidate);
                }
        }
        
        return output;
    }

    private JoinCandidate tryCreateCandidate(SchemaObject object, Kind kind1, Kind kind2, Clause clause, Coloring coloring) {
        final var candidate1 = tryCreateIdRefCandidate(object, kind1, kind2, clause);
        if (candidate1 != null)
            return candidate1;

        final var candidate2 = tryCreateIdRefCandidate(object, kind2, kind1, clause);
        if (candidate2 != null)
            return candidate2;

        // TODO recursion
        return new JoinCandidate(JoinType.Value, kind1, kind2, 0);
    }

    /**
     * This object matches the id-ref join pattern. This means that an object (rootObject) is identified by another object (idObject). The first kind (idKind) has A as a root object and B as a normal property. The second kind (refKind) has the idObject.
     */
    private JoinCandidate tryCreateIdRefCandidate(SchemaObject idObject, Kind idKind, Kind refKind, Clause clause) {
        // First, check if the idObject is an identifier of the root of the idKind.
        final SchemaObject rootObject = idKind.mapping.rootObject();
        if (!rootObject.ids().isSignatures())
            return null;
        // TODO currently, we are using only the first id for joining.
        final SignatureId firstId = rootObject.ids().toSignatureIds().first();
        // TODO currently, we are accepting only signature ids with exactly one signature.
        if (firstId.signatures().size() != 1)
            return null;
        
        final SchemaObject rootIdObject = clause.schema.getEdge(firstId.signatures().first().getLast()).to();
        if (!idObject.equals(rootIdObject))
            return null;

        // The idObject is in fact an identifier of the root of the idKind. We also know that both idKind and refKind contains the object. Therefore we can create the join candidate.
        // TODO recursion
        return new JoinCandidate(JoinType.IdRef, idKind, refKind, 0);
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

    private List<QueryPart2> createQueryParts(List<JoinGroup> groups, List<JoinCandidate> candidatesBetweenParts) {
        // First, we find all candidates between different databases and add them to the `between parts` category.
        groups.stream()
            .filter(g -> !g.databases.isSameDatabase())
            .forEach(g -> candidatesBetweenParts.addAll(g.candidates));
        
        // Then we find all candidates that join kinds from the same db and merge them.
        return groups.stream()
            .filter(g -> g.databases.isSameDatabase())
            .flatMap(g -> mergeNeighbors(g.candidates, candidatesBetweenParts).stream()).toList();
    }

    /**
     * Merges kinds from a single database to a minimal number of query parts.
     */
    private List<QueryPart2> mergeNeighbors(List<JoinCandidate> candidates, List<JoinCandidate> candidatesBetweenParts) {
        // All of the candidates have to have the same database.
        final Database database = candidates.get(0).from().database;
        // If the database supports joins, we use the candidates as edges to construct graph components. Then we create one query part from each component.
        if (database.control.getQueryWrapper().isJoinSupported())
            return GraphUtils.findComponents(candidates).stream().map(c -> QueryPart2.create(c.nodes(), c.edges())).toList();
        
        // Othervise, we have to create custom query part for each kind.
        final var kinds = new TreeSet<Kind>();
        candidates.forEach(c -> {
            kinds.add(c.from());
            kinds.add(c.to());
        });
        // Also, all candidates will join different parts so we add them to the `between parts` category.
        candidatesBetweenParts.addAll(candidates);

        return kinds.stream().map(kind -> QueryPart2.create(kind)).toList();
    }

    private void splitLeaf(Clause clause, List<JoinCandidate> candidates) {
        throw new UnsupportedOperationException();
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