package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.querying.core.Clause;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.QueryPart2;
import cz.matfyz.querying.core.JoinCandidate.JoinProperty;
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
            clause.parts = List.of(new QueryPart2(List.of(kind)));
            return;
        }

        final Coloring coloring = Coloring.create(clause.schema, clause.patternPlan);
        final List<JoinCandidate> joinCandidates = createJoinCandidates(coloring, clause);
        mergeNeighbors(clause, joinCandidates);
        splitLeaf(clause, joinCandidates);
    }

    private List<JoinCandidate> createJoinCandidates(Coloring coloring, Clause clause) {
        final var output = new ArrayList<JoinCandidate>();
        for (final SchemaObject object : coloring.selectMulticolorObjects()) {
            // The set of all objects that have two or more colors.
            final var kinds = coloring.getColors(object).stream().toArray(Kind[]::new);
            // We try each pair of colors.
            for (int i = 0; i < kinds.length; i++)
                for (int j = i + 1; j < kinds.length; j++)
                    output.addAll(tryCreateCandidate(object, kinds[i], kinds[j], clause, coloring));
        }
        
        return output;
    }

    private List<JoinCandidate> tryCreateCandidate(SchemaObject object, Kind kind1, Kind kind2, Clause clause, Coloring coloring) {
        if (!object.ids().isSignatures()) {
            final AccessPath path1 = kind1.mapping.accessPath().tryGetSubpathForObject(object.key(), clause.schema);
            if (path1 == null)
                return List.of();

            final AccessPath path2 = kind2.mapping.accessPath().tryGetSubpathForObject(object.key(), clause.schema);
            if (path2 == null)
                return List.of();

            final var candidate = new JoinCandidate(JoinType.Value, kind1, kind2, List.of(new JoinProperty(path1, path2)), 1);
            return List.of(candidate);
        }

        // Now we have to connect the kinds via all possible ids of given object.
        return object.ids().toSignatureIds().stream()
            .map(id -> tryCreateCandidateFromId(id, kind1, kind2))
            .filter(candidate -> candidate != null)
            .toList();
    }

    private JoinCandidate tryCreateCandidateFromId(SignatureId id, Kind kind1, Kind kind2) {
        final var properties = new ArrayList<JoinProperty>();
        for (final Signature signature : id.signatures()) {
            final AccessPath path1 = kind1.mapping.accessPath().getSubpathBySignature(signature);
            if (path1 == null)
                return null;

            final AccessPath path2 = kind2.mapping.accessPath().getSubpathBySignature(signature);
            if (path2 == null)
                return null;

            properties.add(new JoinProperty(path1, path2));
        }

        return new JoinCandidate(JoinType.IdRef, kind1, kind2, properties, 1);
    }

    private void mergeNeighbors(Clause clause, List<JoinCandidate> candidates) {
        throw new UnsupportedOperationException();
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
                        .computeIfAbsent(edge.dom().key(), x -> new TreeSet<>())
                        .add(kind);

                    objectColors
                        .computeIfAbsent(edge.cod().key(), x -> new TreeSet<>())
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