package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.parsing.WhereClause;

import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class builds the query tree from given clause.
 */
public class QueryTreeBuilder {

    public static QueryNode run(QueryContext context, SchemaCategory originalSchema, List<Kind> allKinds, WhereClause rootClause) {
        return new QueryTreeBuilder(context, originalSchema, allKinds, rootClause).run();
    }

    private final QueryContext context;
    private final SchemaCategory originalSchema;
    private final List<Kind> allKinds;
    private final WhereClause rootClause;

    private QueryTreeBuilder(QueryContext context, SchemaCategory originalSchema, List<Kind> allKinds, WhereClause rootClause) {
        this.context = context;
        this.originalSchema = originalSchema;
        this.allKinds = allKinds;
        this.rootClause = rootClause;
    }

    private QueryNode run() {
        return processClause(rootClause, null);
    }

    private QueryNode processClause(WhereClause clause, @Nullable QueryNode childNode) {
        final var extracted = SchemaExtractor.run(context, originalSchema, allKinds, clause.pattern.triples);
        final List<Set<Kind>> plans = QueryPlanner.run(extracted.schema(), extracted.kinds());
        // TODO better selection?
        final Set<Kind> selectedPlan = plans.get(0);

        QueryNode currentNode = PlanJoiner.run(selectedPlan, extracted.schema());

        for (final var filter : clause.pattern.conditionFilters)
            currentNode = new FilterNode(currentNode, filter);

        for (final var values : clause.pattern.valueFilters)
            currentNode = new FilterNode(currentNode, values);
        
        for (final var nestedClause : clause.nestedClauses)
            currentNode = processClause(nestedClause, currentNode);

        return switch (clause.type) {
            case Where -> currentNode;
            case Minus -> new MinusNode(childNode, currentNode);
            case Optional -> new OptionalNode(childNode, currentNode);
            case Union -> new UnionNode(List.of(childNode, currentNode));
        };
    }

}