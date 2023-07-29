package cz.cuni.matfyz.querying.algorithms;

import cz.cuni.matfyz.abstractwrappers.AbstractStatement;
import cz.cuni.matfyz.abstractwrappers.other.JsonDMLWrapper;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.querying.core.KindDefinition;
import cz.cuni.matfyz.querying.core.QueryEngine;
import cz.cuni.matfyz.querying.core.QueryPlan;
import cz.cuni.matfyz.querying.parsing.Query;
import cz.cuni.matfyz.querying.parsing.QueryParser;
import cz.cuni.matfyz.transformations.algorithms.DMLAlgorithm;

import java.util.List;

/**
 * Given a MMQL `queryString`, execute this query against the given `schemaCategory`.
 * Returns an instance category with the results of the query.
 */
public class QueryToInstance {

    private String queryString;
    private SchemaCategory category;
    private Integer planNumber;
    private List<KindDefinition> kinds;

    public void input(SchemaCategory category, String queryString, Integer planNumber, List<KindDefinition> kinds) {
        this.category = category;
        this.queryString = queryString;
        this.planNumber = planNumber;
        this.kinds = kinds;
    }

    public static record Result(
        InstanceCategory instanceCategory,
        QueryPlan queryPlan,
        List<String> jsonValues
    ) {}

    public Result algorithm() {
        final Query query = QueryParser.parse(queryString);
        
        final Query preprocessedQuery = QueryPreprocessor.preprocessQuery(query);
        final var planner = new QueryPlanner(category, kinds);
        final List<QueryPlan> queryPlans = planner.createPlans(preprocessedQuery);

        
        final QueryPlan bestPlan = planNumber == null
            ? planner.selectBestPlan(queryPlans)
            : queryPlans.get(planNumber);
        
        final var engine = new QueryEngine(category, kinds);
        engine.compileStatements(bestPlan);
        final InstanceCategory whereInstance = engine.executePlan(bestPlan);

        // TODO
        // engine.runDeferredStatements();

        final List<String> jsonResults = createJsonResults(bestPlan, whereInstance);

        return new Result(whereInstance, bestPlan, jsonResults);
    }

    private List<String> createJsonResults(QueryPlan bestPlan, InstanceCategory whereInstance) {
        final var projector = new QueryMappingProjector();
        final var projectionMapping = projector.project(bestPlan, whereInstance);

        final var dmlTransformation = new DMLAlgorithm();
        dmlTransformation.input(projectionMapping, whereInstance, new JsonDMLWrapper());

        return dmlTransformation.algorithm().stream().map(AbstractStatement::getContent).toList();
    }
    
}
