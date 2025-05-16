package cz.matfyz.querying.planner;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.PatternForKind;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.exception.PlanningException;
import cz.matfyz.querying.normalizer.NormalizedQuery.SelectionClause;

import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class builds a query plan from given clause.
 */
public class QueryPlanner {

    public static QueryPlan run(QueryContext context, SchemaCategory originalSchema, List<Mapping> allKinds, SelectionClause rootClause) {
        return new QueryPlanner(context, originalSchema, allKinds, rootClause).run();
    }

    private final QueryContext context;
    private final SchemaCategory originalSchema;
    private final List<Mapping> allKinds;
    private final SelectionClause rootClause;

    private QueryPlanner(QueryContext context, SchemaCategory originalSchema, List<Mapping> allKinds, SelectionClause rootClause) {
        this.context = context;
        this.originalSchema = originalSchema;
        this.allKinds = allKinds;
        this.rootClause = rootClause;
    }

    private QueryPlan run() {
        final var rootNode = processClause(rootClause, null);
        return new QueryPlan(rootNode, context);
    }

    private QueryNode processClause(SelectionClause clause, @Nullable QueryNode childNode) {
        // TODO The QueryContext should be immutable. It should be created for each clause instead of updating the schema category in the same context.
        final var extractedPatterns = SchemaExtractor.run(context, originalSchema, allKinds, clause);
        final List<Set<PatternForKind>> plans = PlanDrafter.run(extractedPatterns);
        if (plans.isEmpty())
            throw PlanningException.noPlans();

        // TODO better selection?
        final Set<PatternForKind> selectedPlan = plans.get(0);

        QueryNode currentNode = PlanJoiner.run(context, selectedPlan, clause.variables());

        for (final var filter : clause.filters())
            currentNode = new FilterNode(currentNode, filter);

        // TODO we are not dealing with the nested clauses now.
        // Probably, each clause should have its own context.
        // for (final var nestedClause : clause.nestedClauses())
        //     currentNode = processClause(nestedClause, currentNode);

        return switch (clause.type()) {
            case Where -> currentNode;
            case Minus -> new MinusNode(childNode, currentNode);
            case Optional -> new OptionalNode(childNode, currentNode);
            case Union -> new UnionNode(childNode, currentNode);
        };
    }

}
