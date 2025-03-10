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
import cz.matfyz.querying.resolver.SelectionResolver;
import cz.matfyz.querying.resolver.queryresult.ResultStructureComputer;
import cz.matfyz.querying.resolver.queryresult.ResultStructureMerger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates and assigns ResultStructures to the nodes of a QueryPlan.
 * The ResultStructure doesn't actually need to be returned, it's mostly just for convenience.
 */
public class ResultStructureResolver implements QueryVisitor<ResultStructure> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionResolver.class);

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
        // TODO: We might be able to just use...
        //   return context.rootStructure();
        // If rootStructure changes during DataSourceTranslator.run(),
        // then maybe try to split the translation so that it doesn't need to be run twice.

        final QueryStatement query = DatasourceTranslator.run(context, node);
        node.structure = query.structure();

        return node.structure;
    }

    public ResultStructure visit(FilterNode node) {
        final var childStructure = node.child().accept(this);

        final var tform = ResultStructureComputer.run(childStructure, node.filter, true);
        LOGGER.debug("Filter transformation:\n{}", tform);

        node.tform = tform;
        node.structure = tform.outputStructure();

        return node.structure;
    }

    public ResultStructure visit(JoinNode node) {
        if (node.candidate.type() == JoinType.Value)
            throw new UnsupportedOperationException("Joining by value is not implemented.");

        // Let's assume that the idRoot is the same as idProperty, i.e., the structure with the id is in the root of the result.
        // TODO Relax this assumption. Probably after we use graph instead of a tree, because we would have to somewhat reorganize the result first.
        // Maybe we can do that simply by turning the parent --> child to child --> array --> parent. Or even just child --> parent if the cardinality is OK?

        // refRoot, idRoot : structures of the complete results returned from children (their "roots")
        final var idRoot = node.fromChild().accept(this);
        final var refRoot = node.toChild().accept(this);

        final var tform = ResultStructureMerger.run(context, idRoot, refRoot, node.candidate.variable());

        node.tform = tform;
        node.structure = tform.newStructure();

        // return tform.apply(idStructure.data, refStructure.data);

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
