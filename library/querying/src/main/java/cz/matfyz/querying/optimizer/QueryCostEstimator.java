package cz.matfyz.querying.optimizer;

import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;
import cz.matfyz.querying.resolver.DatasourceTranslator;

import java.util.ArrayList;

// Maybe change to ResultSizeEstimator; query cost will also depend on query
// execution time, which may not be handled here...? OR it will, and DataModel
// will be more complex with calculateResultSize(), calculateQueryExecutionTime()
// (perhaps for each datasource query (e.g. postgres, mongo, mmcat) independently) etc.

public class QueryCostEstimator implements QueryVisitor<DataModel> {

    private QueryCostEstimator(QueryPlan plan) {
        this.plan = plan;
        this.costsOverNet = new ArrayList<>();
    }

    public static long run(QueryPlan plan) {
        final var estimator = new QueryCostEstimator(plan);
        plan.root.accept(estimator);
        return estimator.costsOverNet.stream().reduce((long)0, (a,b) -> a+b);
    }

    final QueryPlan plan;
    final ArrayList<Long> costsOverNet;

    @Override
    public DataModel visit(DatasourceNode node) {
        try {
            final var query = DatasourceTranslator.run(plan.context, node);

            

            final var collector = plan.context.getProvider().getControlWrapper(node.datasource).getCollectorWrapper();
            final var dataModel = collector.executeQuery(query.content());

            final var stat = dataModel.toResult().resultData().resultTable().getSizeInBytes();

            costsOverNet.add(stat);
            return dataModel;
        } catch (WrapperException e) {
            throw QueryException.message("Something went wrong: " + e.getMessage());
        }

        /*
        final var kindName = projections.get(0).property().mapping.kindName();
        final var cacheEntry = CostData.cacheByKind.get(kindName);

        for (final var projection : projections) {
            final var property = projection.property(); // TODO: CHECK THE PROPERTIES
            final var fieldName = projection.structure().name;
            dataModel.fields.fields.put(fieldName, cacheEntry.fields().get(property));
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
                        dataModel.count = dataModel.count / (max - min + 1);
                        filteredElementData.minValue = value;
                        filteredElementData.maxValue = value;
                        break;
                    case LessOrEqual:
                        if (value < min) {
                            dataModel.count = 0; // or 1 for reserve?
                        } else if (value <= max) {
                            dataModel.count = dataModel.count * (value - min + 1) / (max - min + 1);
                            filteredElementData.maxValue = value;
                        }
                        break;
                    case GreaterOrEqual:
                        if (value > max) {
                            dataModel.count = 0; // or 1 for reserve?
                        } else if (value >= min) {
                            dataModel.count = dataModel.count * (max - value + 1) / (max - min + 1);
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
        */

        // node.kinds.stream().findFirst().get().kind.accessPath(); // TODO: maybe this will be useful for getting the cache

        // costsOverNet.add(stat);
        // return dataModel;
    }

    @Override
    public DataModel visit(FilterNode node) {
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
    public DataModel visit(JoinNode node) {
        node.children().forEach(n -> n.accept(this));
        return null;
    }





    @Override
    public DataModel visit(MinusNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public DataModel visit(OptionalNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public DataModel visit(UnionNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
}
