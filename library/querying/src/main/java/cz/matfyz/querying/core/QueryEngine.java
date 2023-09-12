package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper_old;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper_old.JoinedProperty;
import cz.matfyz.abstractwrappers.utils.PullQuery;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SignatureId;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.exception.InvalidPlanException;
import cz.matfyz.querying.parsing.Filter;
import cz.matfyz.querying.parsing.StringValue;
import cz.matfyz.querying.parsing.Values;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.transformations.processes.DatabaseToInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query engine class which is responsible for the translation of query parts into native queries, and the subsequent execution of those query parts.
 */
@Deprecated
public class QueryEngine {

    private final SchemaCategory schemaCategory;

    public QueryEngine(SchemaCategory schemaCategory) {
        this.schemaCategory = schemaCategory;
    }

    /**
     * For each query part in the given query plan, compile the corresponding native database query, as well as the required mapping for the output of this query.
     * This method saves the compiled queries within each query part, which is why it returns `None`.
     */
    public void compileStatements(QueryPlan plan) {
        for (var part : plan.parts)
            compileQueryPart(part);
    }

    /**
     * Compile the native database query and corresponding mapping for a single query part.
     */
    private void compileQueryPart(QueryPart_old part) {
        var variableTypes = Utils.getVariableTypesFromPart(part, schemaCategory);
        var mappingBuilder = new MappingBuilder();

        var kindsInPart = Utils.getKindsFromPart(part);
        // There is one common wrapper for all kinds in the part.
        var controlWrapper = kindsInPart.get(0).database.control;
        var queryWrapper = controlWrapper.getQueryWrapper_old();

        // for (var kind : kindsInPart)
        //     queryWrapper.defineKind(kind, kind.mapping.kindName());

        processProjectionTriples(part, variableTypes, mappingBuilder, queryWrapper);
        processJoinTriples(part, variableTypes, queryWrapper);
        processFilters(part, queryWrapper);
        processValues(part, queryWrapper);

        var statement = queryWrapper.buildStatement();
        var mapping = mappingBuilder.buildMapping(statement.nameMap());

        part.compiled = new QueryPartCompiled(statement.stringContent(), mapping, controlWrapper);
    }

    private void processProjectionTriples(QueryPart_old part, Map<String, SchemaObject> variableTypes, MappingBuilder mappingBuilder, AbstractQueryWrapper_old queryWrapper) {
        for (var tripleKind : part.triplesMapping) {
            var object = tripleKind.triple.object;

            if (object instanceof StringValue stringObject)
                throw GeneralException.message("Triples with string objects not yet implemented (" + stringObject.value + ").");
            if (!(object instanceof Variable variable) || !Utils.isObjectTerminal(variableTypes.get(variable.name)))
                continue;

            // TODO might be null
            var propertyPath = Utils.getPropertyPath(tripleKind.kind.mapping, tripleKind.triple.signature);
            queryWrapper.addProjection(
                propertyPath,
                tripleKind.kind,
                variable.id
            );
            mappingBuilder.defineVariable(
                variable.id,
                propertyPath,
                tripleKind.kind.mapping
            );
        }
    }

    private void processJoinTriples(QueryPart_old part, Map<String, SchemaObject> variableTypes, AbstractQueryWrapper_old queryWrapper) {
        for (var tripleKindA : part.triplesMapping) {
            for (var tripleKindB : part.triplesMapping) {
                if (!tripleKindA.triple.object.equals(tripleKindB.triple.subject) && !tripleKindA.triple.subject.equals(tripleKindB.triple.subject))
                    continue;

                if (tripleKindA.equals(tripleKindB))
                    continue;
                
                var intersectionVar = tripleKindB.triple.subject;
                var intersectionObject = variableTypes.get(intersectionVar.name);
                SignatureId intersectionIdentifier = null;

                for (var identifier : intersectionObject.ids().toSignatureIds()) {
                    if (identifier.signatures().stream().allMatch(signature -> 
                        signature.toBases().stream().allMatch(base -> Utils.getPropertyPath(tripleKindA.kind.mapping, base) != null)
                    ))
                        intersectionIdentifier = identifier;
                }

                if (intersectionIdentifier == null)
                    throw InvalidPlanException.message("Missing intersection identifier!");

                List<JoinedProperty> joinProperties = new ArrayList<>();

                // TODO the logic might have changed there because of the signature orientation
                for (var signature : intersectionIdentifier.signatures().stream().toList()) {
                    var finalSignature = signature.getFirst();
                    // TODO might be null
                    var lhsPropertyPath = Utils.getPropertyPath(tripleKindA.kind.mapping, finalSignature);
                    var rhsPropertyPath = Utils.getPropertyPath(tripleKindB.kind.mapping, finalSignature);
                    joinProperties.add(new JoinedProperty(lhsPropertyPath, rhsPropertyPath));
                }

                queryWrapper.addJoin(tripleKindA.kind.mapping.kindName(), joinProperties, tripleKindB.kind.mapping.kindName());
            }
        }
    }

    private void processFilters(QueryPart_old part, AbstractQueryWrapper_old queryWrapper) {
        var filters = part.statements.stream().filter(Filter.class::isInstance).map(f -> (Filter) f).toList();

        for (var filter : filters) {
            if (!(filter.lhs instanceof Variable lhsVariable))
                continue;

            if (filter.rhs instanceof StringValue rhsString) {
                queryWrapper.addConstantFilter(
                    lhsVariable.id,
                    filter.operator,
                    rhsString.value
                );
            }
            else if (filter.rhs instanceof Variable rhsVariable) {
                queryWrapper.addVariablesFilter(
                    lhsVariable.id,
                    filter.operator,
                    rhsVariable.id
                );
            }
        }
    }

    private void processValues(QueryPart_old part, AbstractQueryWrapper_old queryWrapper) {
        var valuesFilters = part.statements.stream().filter(Values.class::isInstance).map(v -> (Values) v).toList();
        for (var values : valuesFilters) {
            queryWrapper.addValuesFilter(
                values.variable.id,
                values.allowedValues
            );
        }
    }

    /**
     * Given a query plan with a compiled native query for each of its query parts, execute these native statements and save their results in an instance category in mmcat, returning this instance category.
     * Note that the result instance category corresponds to the results of the query's `WHERE` clause, not the entire query.
     */
    public InstanceCategory executePlan(QueryPlan plan) {

        // TODO - from Merger

        /**
         * Given a `queryPlan` whose query parts have already been compiled, send each native query to mmcat for execution, merge the results into a single instance category and return it.
         * Note that the returned instance category is lazy because of the API of mmcat - it can only return the data for a single instance object or morphism at a time as an API response. For this reason, the instance category retrieves data if it does not yet have it, and then caches the result to eliminate extra API calls.
         */

        InstanceCategory instanceCategory = null;

        for (var part : plan.parts) {
            var databaseToInstance = new DatabaseToInstance();
            databaseToInstance.input(
                part.compiled.mapping,
                instanceCategory,
                part.compiled.controlWrapper.getPullWrapper(),
                PullQuery.fromString(part.compiled.query)
            );
            instanceCategory = databaseToInstance.run();
        }

        // Return instance category with no data - it will be filled in automatically when the data is needed.
        return instanceCategory;
    }

    /**
     * Run all deferred statements which could not be executed natively, as they are either not supported by the underlying database, or they span multiple query parts, which means they could not be executed in the context of a single database.
     */
    public static void runDeferredStatements() {
        // The implementation does not support deferred statements yet.
        throw new UnsupportedOperationException("runDeferredStatements");
    }

}
