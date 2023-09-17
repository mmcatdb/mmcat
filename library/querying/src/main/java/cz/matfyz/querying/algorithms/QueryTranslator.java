package cz.matfyz.querying.algorithms;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.PropertyWithAggregation;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Constant;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.category.Signature;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.KindTree;
import cz.matfyz.querying.core.querytree.DatabaseNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.exception.QueryTreeException;
import cz.matfyz.querying.parsing.Aggregation;
import cz.matfyz.querying.parsing.ConditionFilter;
import cz.matfyz.querying.parsing.StringValue;
import cz.matfyz.querying.parsing.ValueFilter;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.querying.parsing.WhereTriple;
import cz.matfyz.querying.parsing.ParserNode.Term;

import java.util.List;
import java.util.Stack;

/**
 * This class translates a query tree to a query for a specific database.
 * The provided tree has to have `database`, meaning it can be fully resolved withing the given database system.
 */
public class QueryTranslator implements QueryVisitor {

    private final DatabaseNode databaseNode;
    private AbstractQueryWrapper wrapper;

    public QueryTranslator(DatabaseNode databaseNode) {
        this.databaseNode = databaseNode;
    }

    public QueryStatement run() {
        this.wrapper = databaseNode.database.control.getQueryWrapper();
        databaseNode.child.accept(this);

        return this.wrapper.createDSLStatement();
    }

    public void visit(DatabaseNode node) {
        throw QueryTreeException.multipleDatabases(databaseNode.database, node.database);
    }

    public void visit(PatternNode node) {
        node.kinds.forEach(this::processKind);
        node.joinCandidates.forEach(this::processJoinCandidate);
    }

    public void visit(FilterNode node) {
        if (node.filter instanceof ConditionFilter conditionFilter) {
            final var left = createProperty(conditionFilter.lhs);
            final var right = createProperty(conditionFilter.rhs);
            wrapper.addFilter(left, right, conditionFilter.operator);
        }
        else if (node.filter instanceof ValueFilter valueFilter) {
            final var property = createProperty(valueFilter.variable);
            wrapper.addFilter(property, new Constant(valueFilter.allowedValues), ComparisonOperator.Equal);
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

    private static record StackItem(WhereTriple triple, Signature path) {}

    private void processKind(Kind kind) {
        // TODO
        final var kindTree = new KindTree();

        final Stack<StackItem> stack = new Stack<>();
        stack.add(new StackItem(kindTree.rootTriple(), Signature.createEmpty()));
        while (!stack.isEmpty())
            processTopOfStack(stack.pop(), stack, kindTree);
    }

    private void processTopOfStack(StackItem item, Stack<StackItem> stack, KindTree kindTree) {
        final Term object = item.triple.object;
        final Signature path = item.path.concatenate(item.triple.signature);

        final var childTriples = kindTree.getOutgoingTriples(object);
        if (!childTriples.isEmpty()) {
            childTriples.forEach(c -> stack.add(new StackItem(c, path)));
            return;
        }

        // TODO
        final Property subject = new Property(null, null);

        if (object instanceof StringValue constantObject)
            wrapper.addFilter(subject, new Constant(List.of(constantObject.value)), ComparisonOperator.Equal);
        else
            wrapper.addProjection(subject, kindTree.isOptional(item.triple));
    }

    private void processJoinCandidate(JoinCandidate candidate) {
        // TODO
        final Property from = createProperty(null);
        // TODO
        final Property to = createProperty(null);
        wrapper.addJoin(from, to, candidate.recursion(), candidate.isOptional());
    }

    private Property createProperty(Term term) {
        if (term instanceof Variable variable) {
            // TODO
            return new Property(null, null);
        }

        if (term instanceof Aggregation aggregation) {
            final var property = createProperty(aggregation.variable);
            final var root = findAggregationRoot(property.kind, property.path);

            return new PropertyWithAggregation(property.kind, property.path, root, aggregation.operator);
        }

        throw new UnsupportedOperationException();
    }

    private Signature findAggregationRoot(Kind kind, Signature path) {
        // TODO
    }

}