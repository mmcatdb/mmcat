package cz.matfyz.querying.algorithms.translator;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.PropertyWithAggregation;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.ComparisonOperator;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Constant;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.querying.core.QueryContext;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.PatternNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.exception.QueryTreeException;
import cz.matfyz.querying.parsing.Term.Aggregation;
import cz.matfyz.querying.parsing.Filter.ConditionFilter;
import cz.matfyz.querying.parsing.Filter.ValueFilter;
import cz.matfyz.querying.parsing.Term.Variable;
import cz.matfyz.querying.parsing.Term;

/**
 * This class translates a query tree to a query for a specific datasource.
 * The provided tree has to have `datasource`, meaning it can be fully resolved within the given datasource system.
 */
public class QueryTranslator implements QueryVisitor<Void> {

    public static QueryStatement run(QueryContext context, DatasourceNode datasourceNode) {
        return new QueryTranslator(context, datasourceNode).run();
    }

    private final QueryContext context;
    private final DatasourceNode datasourceNode;
    private AbstractQueryWrapper wrapper;

    public QueryTranslator(QueryContext context, DatasourceNode datasourceNode) {
        this.context = context;
        this.datasourceNode = datasourceNode;
    }

    private QueryStatement run() {
        this.wrapper = context.getProvider().getControlWrapper(datasourceNode.datasource).getQueryWrapper();
        datasourceNode.child.accept(this);

        return this.wrapper.createDSLStatement();
    }

    public Void visit(DatasourceNode node) {
        throw QueryTreeException.multipleDatasources(datasourceNode.datasource, node.datasource);
    }

    public Void visit(PatternNode node) {
        PatternTranslator.run(context, node, wrapper);
        return null;
    }

    public Void visit(FilterNode node) {
        if (node.filter instanceof ConditionFilter conditionFilter) {
            final var left = createProperty(conditionFilter.lhs());
            final var right = createProperty(conditionFilter.rhs());
            wrapper.addFilter(left, right, conditionFilter.operator());
        }
        else if (node.filter instanceof ValueFilter valueFilter) {
            final var property = createProperty(valueFilter.variable());
            wrapper.addFilter(property, new Constant(valueFilter.allowedValues()), ComparisonOperator.Equal);
        }

        // propagate (it probably doesn't matter whether before or after processing this node)
        node.child.accept(this);

        return null;
    }

    private Property createProperty(Term term) {
        if (term instanceof Variable variable) {
            // TODO: is the retyping to Variable needed? This can be applied to any term (at least type-wise).
            final var ctx = context.getContext(variable);
            final var mappings = ctx.mappings();
            final var signatures = ctx.signatures();

            if (signatures.size() != 1) {
                throw new UnsupportedOperationException("Cannot choose between multiple possible signatures.");
            }

            return new Property(mappings.get(0), signatures.get(0), null);
        }

        if (term instanceof Aggregation aggregation) {
            final var property = createProperty(aggregation.variable());
            final var root = findAggregationRoot(property.mapping, property.path);

            return new PropertyWithAggregation(property.mapping, property.path, null, root, aggregation.operator());
        }

        throw new UnsupportedOperationException("Can't create property from term: " + term.getClass().getSimpleName() + ".");
    }

    private Signature findAggregationRoot(Mapping kind, Signature path) {
        // TODO
        throw new UnsupportedOperationException("QueryTranslator.findAggregationRoot not implemented.");
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

}
