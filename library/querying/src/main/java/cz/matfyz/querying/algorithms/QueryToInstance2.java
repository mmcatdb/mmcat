package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.abstractwrappers.other.JsonDMLWrapper;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.Clause;
import cz.matfyz.querying.core.QueryEngine;
import cz.matfyz.querying.core.QueryPlan;
import cz.matfyz.querying.parsing.Query;
import cz.matfyz.querying.parsing.QueryParser;
import cz.matfyz.transformations.algorithms.DMLAlgorithm;

import java.util.List;

/**
 * Given a MMQL `queryString`, execute this query against the given `schemaCategory`.
 * Returns an instance category with the results of the query.
 */
public class QueryToInstance2 {

    private String queryString;
    private SchemaCategory schema;
    private List<Kind> kinds;

    public void input(SchemaCategory category, String queryString, List<Kind> kinds) {
        this.schema = category;
        this.queryString = queryString;
        this.kinds = kinds;
    }

    public static record Result(
        InstanceCategory instanceCategory,
        QueryPlan queryPlan,
        List<String> jsonValues
    ) {}

    public Result algorithm() {
        final Query query = QueryParser.parse(queryString);
        processClause(query.whereClause);

        final QueryPlan bestPlan = queryPlans.get(0);
        
        final var engine = new QueryEngine(schema);
        engine.compileStatements(bestPlan);
        final InstanceCategory whereInstance = engine.executePlan(bestPlan);

        // TODO
        // engine.runDeferredStatements();

        final List<String> jsonResults = createJsonResults(bestPlan, whereInstance);

        return new Result(whereInstance, bestPlan, jsonResults);
    }

    private void processClause(Clause clause) {
        final var extracted = new QueryExtractor(schema, kinds, clause.pattern).run();
        clause.schema = extracted.schema();
        clause.kinds = extracted.kinds();

        final var plans = new QueryPlanner2(clause.schema, clause.kinds).run();
        // TODO select plan somehow
        clause.patternPlan = plans.get(0);
        
        clause.nestedClauses.forEach(nestedClause -> processClause(nestedClause));
    }

    private List<String> createJsonResults(QueryPlan bestPlan, InstanceCategory whereInstance) {
        final var projector = new QueryMappingProjector();
        final var projectionMapping = projector.project(bestPlan, whereInstance);

        final var dmlTransformation = new DMLAlgorithm();
        dmlTransformation.input(projectionMapping, whereInstance, new JsonDMLWrapper());

        return dmlTransformation.algorithm().stream().map(AbstractStatement::getContent).toList();
    }
    
}
