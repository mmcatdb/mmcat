package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.PostponedOperation;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.KindTree;
import cz.matfyz.querying.core.filter.ConditionFilter;
import cz.matfyz.querying.core.filter.ValueFilter;
import cz.matfyz.querying.core.querytree.DatabaseNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.exception.QueryTreeException;
import cz.matfyz.querying.parsing.StringValue;
import cz.matfyz.querying.parsing.ValueNode;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.Stack;

/**
 * This class translates a query tree to a query for a specific database.
 * The provided tree has to have `database`, meaning it can be fully resolved withing the given database system.
 */
public class QueryTranslator implements QueryVisitor {

    private static final String PATH_SEPARATOR = "/";

    private final DatabaseNode databaseNode;
    private AbstractQueryWrapper wrapper;

    public QueryTranslator(DatabaseNode databaseNode) {
        this.databaseNode = databaseNode;
    }

    public QueryStatement run() {
        this.wrapper = databaseNode.database.control.getQueryWrapper();
        databaseNode.child.accept(this);

        return this.wrapper.buildStatement();
    }

    public void visit(DatabaseNode node) {
        throw QueryTreeException.multipleDatabases(databaseNode.database, node.database);
    }

    public void visit(PatternNode node) {
        node.kinds.forEach(this::processKind);
        node.joinCandidates.forEach(this::processJoinCandidate);
    }

    public void visit(FilterNode node) {
        if (node.filter instanceof ValueFilter valueFilter) {
            // TODO
        }
        else if (node.filter instanceof ConditionFilter conditionFilter) {
            // TODO
        }
    }

    public void visit(JoinNode node) {
        throw new UnsupportedOperationException();
    }

    public void visit(MinusNode node) {
        throw new UnsupportedOperationException();
    }

    public void visit(OptionalNode node) {
        throw new UnsupportedOperationException();
    }

    public void visit(UnionNode node) {
        throw new UnsupportedOperationException();
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