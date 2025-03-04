package cz.matfyz.querying.resolver;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.querying.QueryResult;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils;
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: deduplicate code from ResultStructureAssigner
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

        final var tform = ResultStructureComputer.run(childResult.structure, node.filter, true);
        LOGGER.debug("Filter transformation:\n{}", tform);

        return tform.apply(childResult.data);
    }

    public QueryResult visit(JoinNode node) {
        if (node.candidate.type() == JoinType.Value)
            throw new UnsupportedOperationException("Joining by value is not implemented.");

        final var idResult = node.fromChild().accept(this);
        final var refResult = node.toChild().accept(this);

        // Let's assume that the idRoot is the same as idProperty, i.e., the structure with the id is in the root of the result.
        // TODO Relax this assumption. Probably after we use graph instead of a tree, because we would have to somewhat reorganize the result first.
        // Maybe we can do that simply by turning the parent --> child to child --> array --> parent. Or even just child --> parent if the cardinality is OK?

        // refRoot, idRoot : structures of the complete results returned from children (their "roots")
        final ResultStructure idRoot = idResult.structure;
        final ResultStructure refRoot = refResult.structure;

        // refMatch, idMatch : an element of the structures which must be equal (e.g. some id) for a join to occur
        final ResultStructure idMatch = findStructure(idRoot, node.candidate.variable());
        final ResultStructure refMatch = findStructure(refRoot, node.candidate.variable());

        // refProperty, idProperty : what SchemaObject will the two results be joined through (it is inferred automatically)
        final ResultStructure refProperty = findParent(idRoot, refRoot, refMatch);

        final var tform = ResultStructureMerger.run(idRoot, refRoot, refProperty, idMatch, refMatch);

        return tform.apply(idResult.data, refResult.data);
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
        // endToStart.remove(endToStart.size() - 1); // apparently root is not included

        for (final var current : endToStart) {
            if (current.variable.equals(child.variable) ||
                context.getSchema().morphismContainsObject(current.signatureFromParent, context.getObjexForVariable(child.variable).key())
            ) {
                return current.parent();
            }
        }

        // return current;
        throw QueryException.message("Could not find a common SchemaObject in a join");
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
