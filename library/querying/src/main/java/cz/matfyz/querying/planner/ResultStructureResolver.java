package cz.matfyz.querying.planner;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils;
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

import java.util.List;

/**
 * Creates and assigns ResultStructures to the nodes of a QueryPlan.
 * The ResultStructure doesn't actually need to be returned, it's mostly just for convenience.
 */
public class ResultStructureResolver implements QueryVisitor<ResultStructure> {

    // private static final Logger LOGGER = LoggerFactory.getLogger(SelectionResolver.class);

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

        final ResultStructure idRoot = idStructure;
        final ResultStructure refRoot = refStructure;

        final ResultStructure idMatch = findStructure(idRoot, node.candidate.variable());
        final ResultStructure refMatch = findStructure(refRoot, node.candidate.variable());

        final ResultStructure refProperty = findParent(idRoot, refRoot, refMatch);

        final var tform = ResultStructureMerger.run(idRoot, refRoot, refProperty, idMatch, refMatch);
        // return tform.apply(idStructure.data, refStructure.data);

        node.structure = tform.newStructure();
        return node.structure;
    }

    private ResultStructure findStructure(ResultStructure root, Variable variable) {
        final var child = GraphUtils.findDFS(root, (node) -> node.variable.equals(variable));
        if (child == null)
            // TODO this should not happen
            throw QueryException.message("ResultStructure not found for variable " + variable);

        return child;
    }

    /** Finds the closest parent of the child structure on the path from the pathStart to the pathEnd (both inclusive). */
    private ResultStructure findParent(ResultStructure child, ResultStructure pathStart, ResultStructure pathEnd) {
        final List<ResultStructure> endToStart = GraphUtils.findPath(pathStart, pathEnd).rootToTarget().reversed();
        ResultStructure current = pathEnd;

        while (!current.equals(pathStart)) {
            if (current.variable.equals(child.variable))
                return current.parent();
            if (context.getSchema().morphismContainsObject(current.signatureFromParent, context.getObjexForVariable(child.variable).key()))
                return current.parent();
        }

        return current;
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
