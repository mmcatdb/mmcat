package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.JoinCondition;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.QueryStructure;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.algorithms.queryresult.QueryStructureMerger;
import cz.matfyz.querying.algorithms.translator.QueryTranslator;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.JoinCandidate.JoinType;
import cz.matfyz.querying.core.querytree.DatabaseNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.exception.QueryTreeException;
import cz.matfyz.querying.parsing.Filter.ConditionFilter;
import cz.matfyz.querying.parsing.Filter.ValueFilter;

import java.util.List;

/**
 * This class translates a query tree to a query for a specific database.
 * The provided tree has to have `database`, meaning it can be fully resolved withing the given database system.
 */
public class QueryResolver implements QueryVisitor<QueryResult> {

    public static QueryResult run(QueryContext context, QueryNode rootNode) {
        return new QueryResolver(context, rootNode).run();
    }

    private final QueryContext context;
    private final QueryNode rootNode;

    private QueryResolver(QueryContext context, QueryNode rootNode) {
        this.context = context;
        this.rootNode = rootNode;
    }

    private QueryResult run() {
        return rootNode.accept(this);
    }

    public QueryResult visit(DatabaseNode node) {
        final QueryStatement query = QueryTranslator.run(context, node);
        final var pullWrapper = node.database.control.getPullWrapper();

        return pullWrapper.executeQuery(query);
    }

    public QueryResult visit(PatternNode node) {
        throw QueryTreeException.unsupportedOutsideDatabase(node);
    }

    public QueryResult visit(FilterNode node) {
        if (node.filter instanceof ConditionFilter conditionFilter) {
            // TODO
            throw new UnsupportedOperationException("QueryResolver.visit(ConditionFilter) not implemented.");
        }
        else if (node.filter instanceof ValueFilter valueFilter) {
            // TODO
            throw new UnsupportedOperationException("QueryResolver.visit(ValueFilter) not implemented.");
        }

        throw new UnsupportedOperationException("QueryResolver.visit(FilterNode) not implemented.");
    }

    public QueryResult visit(JoinNode node) {
        if (node.candidate.type() == JoinType.Value)
            throw new UnsupportedOperationException("Joining by value is not implemented.");

        final JoinCondition condition = node.candidate.joinProperties().getFirst();
        
        final var idResult = node.fromChild.accept(this);
        final var refResult = node.toChild.accept(this);
        
        // Let's assume that the idRoot is the same as idProperty, i.e., the structure with the id is in the root of the result.
        // TODO Relax this assumption. Probably after we use graph instead of a tree, because we would have to somewhat reorganize the result first.
        // Maybe we can do that simply by turning the parent --> child to child --> array --> parent. Or even just child --> parent if the cardinality is OK?
        final QueryStructure idRoot = idResult.structure;
        final QueryStructure refRoot = refResult.structure;
        
        final QueryStructure idMatch = findStructure(idRoot, condition.from());
        final QueryStructure refMatch = findStructure(refRoot, condition.to());

        final QueryStructure refProperty = findParent(idRoot, refRoot, refMatch);
        
        final var tform = QueryStructureMerger.run(idRoot, refRoot, refProperty, idMatch, refMatch);


        System.out.println("source:\n" + tform.sourceTform());
        System.out.println("target:\n" + tform.targetTform());

        System.out.println("\n");

        System.out.println("source:\n" + idRoot);
        System.out.println("target:\n" + refRoot);






     
        return tform.apply(idResult.data, refResult.data);
    }
    
    private QueryStructure findStructure(QueryStructure parent, Signature signature) {
        // TODO This might not work generally when the same schema object is used multiple times in the query. But it should work for now.
        final SchemaObject schemaObject = context.getSchema().getEdge(signature.getLast()).to();
        final QueryStructure output = GraphUtils.findDFS(parent, s -> s.isLeaf() && s.schemaObject.equals(schemaObject));
        if (output == null)
            // TODO this should not happen
            throw QueryException.message("QueryStructure not found for signature " + signature);

        return output;
    }

    /** Finds the closest parent of the child structure on the path from the pathStart to the pathEnd (both inclusive). */
    private QueryStructure findParent(QueryStructure child, QueryStructure pathStart, QueryStructure pathEnd) {
        final List<QueryStructure> endToStart = GraphUtils.findPath(pathStart, pathEnd).rootToTarget().reversed();
        QueryStructure current = pathEnd;

        while (!current.equals(pathStart)) {
            if (current.schemaObject.equals(child.schemaObject))
                return current.parent();
            if (context.getSchema().morphismContainsObject(current.signatureFromParent, child.schemaObject.key()))
                return current.parent();
        }

        return current;
    }

    public QueryResult visit(MinusNode node) {
        throw new UnsupportedOperationException("QueryResolver.visit(MinusNode) not implemented.");
    }

    public QueryResult visit(OptionalNode node) {
        throw new UnsupportedOperationException("QueryResolver.visit(OptionalNode) not implemented.");
    }

    public QueryResult visit(UnionNode node) {
        throw new UnsupportedOperationException("QueryResolver.visit(UnionNode) not implemented.");
    }

}
