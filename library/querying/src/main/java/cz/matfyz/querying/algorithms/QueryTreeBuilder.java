package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.RootNode;
import cz.matfyz.querying.exception.QueryTreeException;
import cz.matfyz.querying.parsing.WhereClause;

import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class builds the query tree from given clause.
 */
public class QueryTreeBuilder {

    public static RootNode run(SchemaCategory originalSchema, List<Kind> allKinds, WhereClause rootClause) {
        return new QueryTreeBuilder(originalSchema, allKinds, rootClause).run();
    }

    private final SchemaCategory originalSchema;
    private final List<Kind> allKinds;
    private final WhereClause rootClause;

    private QueryTreeBuilder(SchemaCategory originalSchema, List<Kind> allKinds, WhereClause rootClause) {
        this.originalSchema = originalSchema;
        this.allKinds = allKinds;
        this.rootClause = rootClause;
    }

    private RootNode run() {
        final var node = processClause(rootClause, null);
        if (!(node instanceof RootNode rootNode))
            throw QueryTreeException.nodeNotRoot(node);

        return rootNode;
    }

    private QueryNode processClause(WhereClause clause, @Nullable QueryNode childNode) {
        final var extracted = SchemaExtractor.run(originalSchema, allKinds, clause.pattern.triples);
        final List<Set<Kind>> plans = QueryPlanner.run(extracted.schema(), extracted.kinds());
        // TODO better selection?
        final Set<Kind> selectedPlan = plans.get(0);

        QueryNode currentNode = PlanJoiner.run(selectedPlan, extracted.schema());

        for (final var filter : clause.pattern.conditionFilters)
            currentNode = new FilterNode(childNode, filter);

        for (final var values : clause.pattern.valueFilters)
            currentNode = new FilterNode(childNode, values);
        
        for (final var nestedClause : clause.nestedClauses)
            currentNode = processClause(nestedClause, currentNode);

        return switch (clause.type) {
            case Where -> new RootNode(currentNode);
            case Minus -> new MinusNode(childNode, currentNode);
            case Optional -> new MinusNode(childNode, currentNode);
            case Union -> new MinusNode(childNode, currentNode);
        };
    }

}