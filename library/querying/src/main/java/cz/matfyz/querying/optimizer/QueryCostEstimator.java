package cz.matfyz.querying.optimizer;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.Property;
import cz.matfyz.core.querying.Computation.OP;
import cz.matfyz.core.querying.Computation.Operator;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.optimizer.CostData.KindCostData;
import cz.matfyz.querying.optimizer.CostData.ObjectCostData;
import cz.matfyz.querying.optimizer.CostData.ScalarCostData;
import cz.matfyz.querying.planner.QueryPlan;

import java.util.ArrayList;
import java.util.stream.Collectors;

// Maybe change to ResultSizeEstimator; query cost will also depend on query
// execution time, which may not be handled here...? OR it will, and KindCostData
// will be more complex with calculateResultSize(), calculateQueryExecutionTime()
// (perhaps for each datasource query (e.g. postgres, mongo, mmcat) independently) etc.

public class QueryCostEstimator implements QueryVisitor<KindCostData> {

    private QueryCostEstimator(QueryPlan plan) {
        this.plan = plan;
        this. costsOverNet = new ArrayList<>();
    }

    public static int run(QueryPlan plan) {
        final var estimator = new QueryCostEstimator(plan);
        plan.root.accept(estimator);
        return estimator.costsOverNet.stream().map(c -> c.estimateResultSize()).reduce(0, (a,b) -> a+b);
    }

    final QueryPlan plan;
    final ArrayList<KindCostData> costsOverNet;

    @Override
    public KindCostData visit(DatasourceNode node) {
        // starting with MongoDB, expect just one join candidate
        // use projections to see what element has what objex key

        final var rv = DatasourceTranslatorForCost.run(plan.context, node);
        final var projections = rv.projections();
        final var wrapperContext = rv.wrapperContext();

        final var kindName = projections.get(0).property().mapping.kindName();
        final var cacheEntry = CostData.cacheByKind.get(kindName);

        final var kindCostData = new KindCostData();
        kindCostData.count = cacheEntry.count();
        kindCostData.fields = new ObjectCostData();

        for (final var projection : projections) {
            final var property = projection.property(); // TODO: CHECK THE PROPERTIES
            final var fieldName = projection.structure().name;
            kindCostData.fields.fields.put(fieldName, cacheEntry.fields().get(property));
        }

        for (final var filter : node.filters) {
            if (filter.operator.isComparison() &&
                filter.arguments.get(0) instanceof Variable &&
                filter.arguments.get(1) instanceof Constant
            ) {
                // FIXME for now we just assume the reference node is root
                Variable var = (Variable)(filter.arguments.get(0));
                Constant con = (Constant)(filter.arguments.get(1));

                Property property = wrapperContext.getProperty(var);
                ScalarCostData filteredElementData = (ScalarCostData)(cacheEntry.fields().get(property));

                final var min = filteredElementData.minValue;
                final var max = filteredElementData.maxValue;
                int value = Integer.parseInt(con.value());

                final var operator = filter.operator;
                switch (operator) {
                    case Equal:
                        kindCostData.count = kindCostData.count / (max - min + 1);
                        filteredElementData.minValue = value;
                        filteredElementData.maxValue = value;
                        break;
                    case LessOrEqual:
                        if (value < min) {
                            kindCostData.count = 0; // or 1 for reserve?
                        } else if (value <= max) {
                            kindCostData.count = kindCostData.count * (value - min + 1) / (max - min + 1);
                            filteredElementData.maxValue = value;
                        }
                        break;
                    case GreaterOrEqual:
                        if (value > max) {
                            kindCostData.count = 0; // or 1 for reserve?
                        } else if (value >= min) {
                            kindCostData.count = kindCostData.count * (max - value + 1) / (max - min + 1);
                            filteredElementData.minValue = value;
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Only comparison operators are supported in cost estimation for now.");
                };
            } else {
                throw new UnsupportedOperationException("Only comparison operators are supported in cost estimation for now.");
            }

        }

        // node.kinds.stream().findFirst().get().kind.accessPath(); // TODO: maybe this will be useful for getting the cache

        costsOverNet.add(kindCostData);
        return kindCostData;
    }

    @Override
    public KindCostData visit(FilterNode node) {
        node.child().accept(this);
        return null;

        // final var cost = node.child().accept(this);
        // find the reference node (or assume it is root ?)
        // find signature to the filtered property
        // estimate which portion will get filtered
        // adjust the root count value accordingly (I assume it is an array, so avgCount, or root count)

        // find out what is being filtered, and update the data...
    }

    @Override
    public KindCostData visit(JoinNode node) {
        node.children().forEach(n -> n.accept(this));
        return null;
    }





    @Override
    public KindCostData visit(MinusNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public KindCostData visit(OptionalNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public KindCostData visit(UnionNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}
