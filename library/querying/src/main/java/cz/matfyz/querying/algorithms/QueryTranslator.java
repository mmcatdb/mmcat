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
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.patterntree.KindPattern;
import cz.matfyz.querying.core.patterntree.PatternObject;
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
import cz.matfyz.querying.parsing.ParserNode.Term;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * This class translates a query tree to a query for a specific database.
 * The provided tree has to have `database`, meaning it can be fully resolved withing the given database system.
 */
public class QueryTranslator implements QueryVisitor<Void> {

    public static QueryStatement run(QueryContext context, DatabaseNode databaseNode) {
        return new QueryTranslator(context, databaseNode).run();
    }

    private final QueryContext context;
    private final DatabaseNode databaseNode;
    private AbstractQueryWrapper wrapper;

    public QueryTranslator(QueryContext context, DatabaseNode databaseNode) {
        this.context = context;
        this.databaseNode = databaseNode;
    }

    public QueryStatement run() {
        this.wrapper = databaseNode.database.control.getQueryWrapper();
        databaseNode.child.accept(this);

        return this.wrapper.createDSLStatement();
    }

    public Void visit(DatabaseNode node) {
        throw QueryTreeException.multipleDatabases(databaseNode.database, node.database);
    }

    public Void visit(PatternNode node) {
        wrapper.defineRoot(node.rootTerm.getIdentifier());

        node.kinds.forEach(this::processKind);
        node.joinCandidates.forEach(this::processJoinCandidate);

        return null;
    }

    public Void visit(FilterNode node) {
        if (node.filter instanceof ConditionFilter conditionFilter) {
            final var left = createProperty(conditionFilter.lhs);
            final var right = createProperty(conditionFilter.rhs);
            wrapper.addFilter(left, right, conditionFilter.operator);
        }
        else if (node.filter instanceof ValueFilter valueFilter) {
            final var property = createProperty(valueFilter.variable);
            wrapper.addFilter(property, new Constant(valueFilter.allowedValues), ComparisonOperator.Equal);
        }

        return null;
    }

    public Void visit(JoinNode node) {
        throw new UnsupportedOperationException("QueryTranslator.visit(JoinNode) not implemented.");
    }

    public Void visit(MinusNode node) {
        throw new UnsupportedOperationException("QueryTranslator.visit(MinusNode) not implemented.");
    }

    public Void visit(OptionalNode node) {
        throw new UnsupportedOperationException("QueryTranslator.visit(OptionalNode) not implemented.");
    }

    public Void visit(UnionNode node) {
        throw new UnsupportedOperationException("QueryTranslator.visit(UnionNode) not implemented.");
    }

    private static record StackItem(PatternObject object, Signature path) {}

    private void processKind(KindPattern kind) {
        // TODO this is weird, because it's happening for each kind - meaning that the last one overrides all the previous ones
        // wrapper.defineRoot(kind.root.schemaObject, kind.root.term.getIdentifier());

        final Deque<StackItem> stack = new ArrayDeque<>();
        stack.push(new StackItem(kind.root, Signature.createEmpty()));
        while (!stack.isEmpty())
            processTopOfStack(kind, stack.pop(), stack);
    }

    private void processTopOfStack(KindPattern kind, StackItem item, Deque<StackItem> stack) {
        if (!item.object.isTerminal()) {
            item.object.children().forEach(child -> stack.push(new StackItem(child, item.path.concatenate(child.signatureFromParent()))));
            return;
        }
        
        final Term term = item.object.term;
        if (term == null)
            return;

        final var objectProperty = new Property(kind.kind, item.path);

        if (term instanceof StringValue constantObject)
            wrapper.addFilter(objectProperty, new Constant(List.of(constantObject.value)), ComparisonOperator.Equal);
        else
            // TODO isOptional is not supported yet.
            wrapper.addProjection(objectProperty, term.getIdentifier(), false);
    }

    private void processJoinCandidate(JoinCandidate candidate) {
        // // TODO
        // final Property from = createProperty(null);
        // // TODO
        // final Property to = createProperty(null);
        wrapper.addJoin(candidate.from().kind, candidate.to().kind, candidate.joinProperties(), candidate.recursion(), candidate.isOptional());
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

        throw new UnsupportedOperationException("Can't create property from term: " + term.getClass().getSimpleName() + ".");
    }

    private Signature findAggregationRoot(Kind kind, Signature path) {
        // TODO
        throw new UnsupportedOperationException("QueryTranslator.findAggregationRoot not implemented.");
    }

}