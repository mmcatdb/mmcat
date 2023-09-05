package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.querying.core.QueryPart;
import cz.matfyz.querying.core.QueryPlan;
import cz.matfyz.querying.core.TripleKind;
import cz.matfyz.querying.core.Utils;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.exception.InvalidPlanException;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Query planner class which is responsible for the creation of query plans for a given query.
 */
public class QueryPlanner {

    private final SchemaCategory schema;
    private final List<Kind> allKinds;

    public QueryPlanner(SchemaCategory schema, List<Kind> allKinds) {
        this.schema = schema;
        this.allKinds = allKinds;
    }

    /**
     * Given an input query, generate all possible query plans.
     * In the case of no data redundancy, the result will always contain only one query plan. In the case that redundancy is present, there can be multiple plans.
     */
    public List<QueryPlan> createPlans(Query query) {
        final var variableTypes = Utils.getVariableTypesFromQuery(query, schema);
        final var usedSchemaObjectKeys = new TreeSet<Key>();
        variableTypes.values().forEach(schemaObject -> usedSchemaObjectKeys.add(schemaObject.key()));
        
        final var tripleKindsAssignments = new ArrayList<List<TripleKind>>();

        for (final WhereTriple triple : query.where.triples) {
            final var selectedKinds = allKinds.stream().filter(k ->
                Utils.getPropertyPath(k.mapping, triple.signature) != null && usedSchemaObjectKeys.contains(k.mapping.rootObject().key())
            ).toList();
            if (selectedKinds.isEmpty())
                throw GeneralException.message("Cannot create query plan - morphism not in mapping or root object not in mapping");

            final List<TripleKind> assignments = selectedKinds.stream().map(k -> new TripleKind(triple, k)).toList();
            tripleKindsAssignments.add(assignments);
        }

        final var assignmentsProduct = cartesianProduct(tripleKindsAssignments);
        final var queryPlans = new ArrayList<QueryPlan>();
        
        for (final var assignment : assignmentsProduct) {
            try {
                final QueryPlan queryPlan = createPlanFromAssignment(query, variableTypes, assignment);
                queryPlans.add(queryPlan);
            }
            catch (InvalidPlanException e) {
                // TODO - what here?
            }
        }

        return queryPlans;
    }

    private static <T> List<List<T>> cartesianProduct(List<List<T>> input) {
        if (input.size() < 2)
            return input;

        var firstList = input.get(0);
        var rest = input.subList(1, input.size());
        var restPoduct = cartesianProduct(rest);

        var output = new ArrayList<List<T>>();
        for (var item : firstList) {
            for (var list : restPoduct) {
                var newList = new ArrayList<T>();
                newList.add(item);
                newList.addAll(list);
                output.add(newList);
            }
        }

        return output;
    }

    private QueryPlan createPlanFromAssignment(Query query, Map<String, SchemaObject> variableTypes, List<TripleKind> assignment) {
        var initialQueryPart = new QueryPart(assignment.stream().toList(), new ArrayList<>());

        var finishedQueryParts = new ArrayList<QueryPart>();
        var queryPartQueue = new LinkedList<QueryPart>();
        queryPartQueue.add(initialQueryPart);

        while (!queryPartQueue.isEmpty()) {
            var queryPart = queryPartQueue.pop();
            var tmpSet = new TreeSet<>(queryPart.triplesMapping.stream().map(tm -> tm.kind.database).toList());
            if (tmpSet.size() == 1) {
                finishedQueryParts.add(queryPart);
                continue;
            }

            var splitQueryParts = splitSingleQueryPart(variableTypes, queryPart);
            queryPartQueue.addAll(splitQueryParts);
        }

        assignStatementsToParts(query, finishedQueryParts);
        var cost = finishedQueryParts.size() * 100;

        return new QueryPlan(query, finishedQueryParts, new ArrayList<>(), cost);
    }

    /**
     * Match triples pattern () -A-> (I) -B-> () or () <-A- (I) -B-> ()
     */
    private List<QueryPart> splitSingleQueryPart(Map<String, SchemaObject> variableTypes, QueryPart queryPart) {
        for (var tripleKindA : queryPart.triplesMapping) {
            for (var tripleKindB : queryPart.triplesMapping) {
                // This condition needs to change in the cases of databases without joins, but mmcat doesn't support joins yet anyway.
                if (tripleKindA.kind.database.equals(tripleKindB.kind.database))
                    continue;

                if (
                    !tripleKindA.triple.object.equals(tripleKindB.triple.subject)
                        && !tripleKindA.triple.subject.equals(tripleKindB.triple.subject)
                )
                    continue;

                return splitJoinPoint(variableTypes, queryPart, tripleKindA.triple, tripleKindA.kind, tripleKindB.triple, tripleKindB.kind);
            }
        }

        throw InvalidPlanException.message("Missing join point");
    }

    static record QueryPartKind(
        QueryPart queryPart,
        Kind kind
    ) {}

    private List<QueryPart> splitJoinPoint(
        Map<String, SchemaObject> variableTypes,
        QueryPart queryPart,
        WhereTriple tripleA,
        Kind kindA,
        WhereTriple tripleB,
        Kind kindB
    ) {
        var intersectionVariable = tripleB.subject;
        var intersectionObject = variableTypes.get(intersectionVariable.name);
        SignatureId intersectionIdentifier = null;

        for (var identifier : intersectionObject.ids().toSignatureIds()) {
            if (identifier.signatures().stream().allMatch(signature -> 
                signature.toBases().stream().allMatch(base -> Utils.getPropertyPath(kindA.mapping, base) != null)
            ))
                intersectionIdentifier = identifier;
        }

        // Project identifier from both kinds and split query part
        if (intersectionIdentifier == null)
            throw InvalidPlanException.message("Well that's a shame (" + tripleA.signature + ", " + tripleB + ")");

        // When mmcat supports joins and we can implement them, non-contiguous database parts (like mongo-postgre-mongo) could leave gaps in the query parts with this implementation.
        var triplesMappingA = queryPart.triplesMapping.stream().filter(tm -> tm.kind.database.equals(kindB.database)).toList();
        var triplesMappingB = queryPart.triplesMapping.stream().filter(tm -> !triplesMappingA.contains(tm)).toList();

        var queryPartA = new QueryPart(triplesMappingA, new ArrayList<>());
        var queryPartB = new QueryPart(triplesMappingB, new ArrayList<>());

        var tmp = List.of(
            new QueryPartKind(queryPartA, kindA),
            new QueryPartKind(queryPartB, kindB)
        );

        for (var queryPartKind : tmp) {
            for (var signature : intersectionIdentifier.signatures()) {
                // In the case that an identifier is a compound signature, this will not suffice.
                var morphismSignature = signature.getFirst();
                var anyTriples = queryPartKind.queryPart.triplesMapping.stream().anyMatch(tm ->
                    tm.triple.subject.name.equals(intersectionVariable.name) && tm.triple.signature.equals(morphismSignature)
                );
                if (!anyTriples) {
                    // Note that the variable name will be different for both query parts, but this is not a problem since the morphisms dictate data placement when joining instance categories.
                    var newTriple = new WhereTriple(intersectionVariable, morphismSignature, Variable.generated());
                    queryPartKind.queryPart.triplesMapping.add(new TripleKind(newTriple, queryPartKind.kind));
                }
            }
        }

        return List.of(queryPartA, queryPartB);
    }

    /**
     * Given a list of finished query parts for a query plan, go through the non-triple statements in the query and assign them to query parts.
     * Note that a single statement may be assigned to multiple query parts, for example filtering the value of a variable which is selected from multiple query parts must naturally apply this filter to all relevant query parts.
     */
    private void assignStatementsToParts(Query query, List<QueryPart> parts) {
        for (var part : parts) {
            for (var filter : query.where.filters) {
                for (var tripleKind : part.triplesMapping) {
                    // Whenever we implement deferred statements, we will need to check whether a potential rhs variable/aggregation is in the same query part.
                    if (filter.lhs instanceof Variable variable && variable.equals(tripleKind.triple.object))
                        part.statements.add(filter);
                }
            }

            for (var values : query.where.values)
                for (var tripleKind : part.triplesMapping)
                    if (values.variable.equals(tripleKind.triple.object))
                        part.statements.add(values);
        }
    }

    /**
     * Given a set of query plans, evaluate the cost of each plan and return the best plan.
     */
    public QueryPlan selectBestPlan(List<QueryPlan> plans) {
        // Selection of the best plan is outside the scope of my thesis,
        // but I will probably soon add some basic algorithm for this.
        return plans.get(0);
    }

}