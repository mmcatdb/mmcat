package cz.matfyz.querying.planner;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.resolver.DatasourceTranslator;
import cz.matfyz.querying.resolver.queryresult.ResultStructureMerger;

/**
 * Creates and assigns ResultStructures to the nodes of a QueryPlan.
 * The ResultStructure doesn't actually need to be returned, it's mostly just for convenience.
 */
public class ResultStructureResolver implements QueryVisitor<ResultStructure> {

    public static void run(QueryPlan plan) {
        new ResultStructureResolver(plan.context, plan.root).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private ResultStructureResolver(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private void run() {
        rootNode.accept(this);
    }

    public ResultStructure visit(DatasourceNode node) {
        // TODO: or just use its context.rootStructure()
        final QueryStatement query = DatasourceTranslator.run(context, node);
        node.structure = query.structure();

        // final var pullWrapper = context.getProvider().getControlWrapper(node.datasource).getPullWrapper();

        // pullWrapper.executeQuery(query);
        return node.structure;
    }

    public ResultStructure visit(FilterNode node) {
        final var childStructure = node.child().accept(this);

        // final var tform = ResultStructureComputer.run(childStructure, node.filter, true);
        // LOGGER.debug("Filter transformation:\n{}", tform);

        // tform.apply(childStructure.data);

        node.structure = childStructure.copy();
        return node.structure;
    }

    public ResultStructure visit(JoinNode node) {
        if (node.candidate.type() == JoinType.Value)
            throw new UnsupportedOperationException("Joining by value is not implemented.");

        final var idStructure = node.fromChild().accept(this);
        final var refStructure = node.toChild().accept(this);

        final var tform = ResultStructureMerger.run(context, idStructure, refStructure, node.candidate.variable());
        // return tform.apply(idStructure.data, refStructure.data);

        node.structure = tform.newStructure();
        return node.structure;
    }

    public ResultStructure visit(MinusNode node) {
        throw new UnsupportedOperationException("ResultStructureAssigner.visit(MinusNode) not implemented.");
    }

    public ResultStructure visit(OptionalNode node) {
        throw new UnsupportedOperationException("ResultStructureAssigner.visit(OptionalNode) not implemented.");
    }

    public ResultStructure visit(UnionNode node) {
        throw new UnsupportedOperationException("ResultStructureAssigner.visit(UnionNode) not implemented.");
    }
}
