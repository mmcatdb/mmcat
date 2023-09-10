package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.PostponedOperation;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.KindTree;
import cz.matfyz.querying.core.QueryPart;
import cz.matfyz.querying.parsing.StringValue;
import cz.matfyz.querying.parsing.ValueNode;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.Stack;

/**
 * This class translates a query part to a query for a specific database.
 */
public class QueryPartTranslator {

    private static final String PATH_SEPARATOR = "/";

    private final QueryPart queryPart;
    private final AbstractQueryWrapper wrapper;

    public QueryPartTranslator(QueryPart queryPart) {
        this.queryPart = queryPart;
        this.wrapper = queryPart.database().control.getQueryWrapper();
    }

    public void run() {
        queryPart.kinds().forEach(this::processKind);
        queryPart.joinCandidates().forEach(this::processJoinCandidate);
        // TODO
        // queryPart.valueStatements().forEach(this::processValueStatement);
        // queryPart.filterStatements().forEach(this::processFilterStatement);
    }

    private static record StackItem(WhereTriple triple, String path) {}

    private void processKind(Kind kind) {
        wrapper.pushKind(kind);

        final var kindTree = new KindTree(); // TODO

        final Stack<StackItem> stack = new Stack<>();
        stack.add(new StackItem(kindTree.rootTriple(), ""));
        while (!stack.isEmpty())
            processTopOfStack(stack.pop(), stack, kindTree);

        wrapper.popKind();
    }

    private void processTopOfStack(StackItem item, Stack<StackItem> stack, KindTree kindTree) {
        final ValueNode object = item.triple.object;
        final String path = item.path + PATH_SEPARATOR + object.name();

        final var childTriples = kindTree.getOutgoingTriples(object);
        if (!childTriples.isEmpty()) {
            childTriples.forEach(c -> stack.add(new StackItem(c, path)));
        }
        else if (object instanceof StringValue constantObject) {
            processOrPostponeFiltering(
                new Operand(OperandType.Variable, kindTree.kind(), item.triple, path, null),
                new Operand(OperandType.Constant, null, null, constantObject.value, null),
                ComparisonOperator.Equal,
                PostponedOperation.Filtering
            );
        }
        else {
            wrapper.addProjection(path, kindTree.isOptional(item.triple));
        }
    }

    private void processJoinCandidate(JoinCandidate candidate) {
        if (candidate.isRecursive())
            wrapper.addRecursiveJoinOrPostpone(candidate.from(), candidate.to(), candidate.match(), candidate.recursion());
        else if (candidate.isOptional())
            wrapper.addOptionalJoinOrPostpone(candidate.from(), candidate.to(), candidate.match());
        else
            wrapper.addJoin(candidate.from(), candidate.to(), candidate.match());
    }

    private void processValueStatement(Object value) {

    }

    private void processFilterStatement(Object filter) {

    }

    private static enum OperandType {
        Variable,
        Constant,
        Aggregation,
    }

    private static record Operand(
        OperandType type,
        Kind kind,
        WhereTriple triple,
        String path,
        Object aggregationRoot
    ) {}

    private void processOrPostponeFiltering(Operand lhs, Operand rhs, ComparisonOperator operator, PostponedOperation postponedOperation) {
        if (lhs.aggregationRoot == null) {
            wrapper.addFilterOrPostpone(lhs.kind, lhs.path, rhs.kind, rhs.path, operator, postponedOperation);
        }
        else if (rhs.aggregationRoot == null) {
            wrapper.addFilterOrPostpone(lhs.kind, lhs.path, rhs.kind, rhs.path, operator, postponedOperation, lhs.aggregationRoot);
        }
        else {
            wrapper.addFilterOrPostpone(lhs.kind, lhs.path, rhs.kind, rhs.path, operator, postponedOperation, lhs.aggregationRoot, rhs.aggregationRoot);
        }
    }

}