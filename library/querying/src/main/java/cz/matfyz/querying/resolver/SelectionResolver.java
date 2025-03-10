package cz.matfyz.querying.resolver;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.resolver.queryresult.ResultStructureComputer;
import cz.matfyz.querying.resolver.queryresult.ResultStructureMerger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Maybe replace QueryResult for ListResult?
public class SelectionResolver implements QueryVisitor<QueryResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionResolver.class);

    public static QueryResult run(QueryPlan plan) {
        return new SelectionResolver(plan.context, plan.root).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private SelectionResolver(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private QueryResult run() {
        return rootNode.accept(this);
    }

    public QueryResult visit(DatasourceNode node) {
        final QueryStatement query = DatasourceTranslator.run(context, node);
        final var pullWrapper = context.getProvider().getControlWrapper(node.datasource).getPullWrapper();

        return pullWrapper.executeQuery(query);
    }

    public QueryResult visit(FilterNode node) {
        final var childResult = node.child().accept(this);
        return node.tform.apply(childResult.data);
    }

    public QueryResult visit(JoinNode node) {
        if (node.candidate.type() == JoinType.Value)
            throw new UnsupportedOperationException("Joining by value is not implemented.");

        final var idResult = node.fromChild().accept(this);
        final var refResult = node.toChild().accept(this);

        return node.tform.apply(idResult.data, refResult.data);
    }

    public QueryResult visit(MinusNode node) {
        throw new UnsupportedOperationException("SelectionResolver.visit(MinusNode) not implemented.");
    }

    public QueryResult visit(OptionalNode node) {
        throw new UnsupportedOperationException("SelectionResolver.visit(OptionalNode) not implemented.");
    }

    public QueryResult visit(UnionNode node) {
        throw new UnsupportedOperationException("SelectionResolver.visit(UnionNode) not implemented.");
    }

}
