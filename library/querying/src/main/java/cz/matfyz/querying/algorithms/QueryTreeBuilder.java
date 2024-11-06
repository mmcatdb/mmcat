package cz.matfyz.querying.algorithms;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.KindPattern;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.exception.PlanningException;
import cz.matfyz.querying.parsing.WhereClause;

import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class builds the query tree from given clause.
 */
public class QueryTreeBuilder {

    public static QueryNode run(QueryContext context, SchemaCategory originalSchema, List<Mapping> allKinds, WhereClause rootClause) {
        return new QueryTreeBuilder(context, originalSchema, allKinds, rootClause).run();
    }

    private final QueryContext context;
    private final SchemaCategory originalSchema;
    private final List<Mapping> allKinds;
    private final WhereClause rootClause;

    private QueryTreeBuilder(QueryContext context, SchemaCategory originalSchema, List<Mapping> allKinds, WhereClause rootClause) {
        this.context = context;
        this.originalSchema = originalSchema;
        this.allKinds = allKinds;
        this.rootClause = rootClause;
    }

    private QueryNode run() {
        return processClause(rootClause, null);
    }

    private QueryNode processClause(WhereClause clause, @Nullable QueryNode childNode) {
        // TODO The QueryContext should be immutable. It should be created for each clause instead of updating the schema category in the same context.
        final var extractedPatterns = SchemaExtractor.run(context, originalSchema, allKinds, clause);
        final List<Set<KindPattern>> plans = QueryPlanner.run(extractedPatterns);
        if (plans.isEmpty())
            throw PlanningException.noPlans();

        // TODO better selection?
        final Set<KindPattern> selectedPlan = plans.get(0);

        QueryNode currentNode = PlanJoiner.run(context, selectedPlan, clause.termTree);

        for (final var filter : clause.conditionFilters)
            currentNode = new FilterNode(currentNode, filter);

        for (final var values : clause.valueFilters)
            currentNode = new FilterNode(currentNode, values);

        // TODO we are not dealing with the nested clauses now.
        // Probably, each clause should have its own context.
        // for (final var nestedClause : clause.nestedClauses)
        //     currentNode = processClause(nestedClause, currentNode);

        return switch (clause.type) {
            case Where -> currentNode;
            case Minus -> new MinusNode(childNode, currentNode);
            case Optional -> new OptionalNode(childNode, currentNode);
            case Union -> new UnionNode(List.of(childNode, currentNode));
        };
    }

}
