package cz.matfyz.querying.optimizer;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.Expression.Constant;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A persistent query-driven data structure for storing query result information.
 * For now, it is implemented as a very simple cache of query -> result size & time,
 * but can be more than that.
 * 
 * This originally belonged into `core`, but needs the QueryPlan class to work. TODO: Maybe reconcile this dependency problem
 */
public class CollectorCache {

    public final Map<String, DataModel.DatabaseData> databaseData = new HashMap<>();

    public final Map<String, ArrayList<CacheEntry>> queryData = new HashMap<>();

    static final int MAX_COUNT = 20_000;

    public void put(DatasourceNode datasourceNode, DataModel data) {
        if (queryData.size() >= MAX_COUNT) {
            queryData.clear(); // Or theoretically halve the size, but who cares
        }

        final var planKey = PlanToCacheKeyConverter.run(datasourceNode);
        var results = queryData.get(planKey);
        if (results == null) {
            results = new ArrayList<CacheEntry>();
            queryData.put(planKey, results);
        }
        results.add(new CacheEntry(datasourceNode, data));
    }

    public NodeCostData predict(QueryNode node) {
        if (node instanceof DatasourceNode dsNode) return predict(dsNode);
        // TODO: expand using joins
        return null;
    }
    public NodeCostData predict(DatasourceNode node) {
        final var key = PlanToCacheKeyConverter.run(node);
        final var entries = queryData.get(key);

        if (entries == null || entries.size() == 0) return null;

        final var comparisons = entries.stream().map(entry -> {
            final var comp = compare(node, entry.node);
            // return new ConcreteComparison(comp, entry.dataModel.result.resultTable.sizeInBytes);
            return new ConcreteComparison(comp, entry.node.evalData);
        }).toList();

        Double minTime = null, maxTime = null;
        Double minRows = null, maxRows = null;
        for (final var comp : comparisons) {
            if (comp.comparison == Comparison.More) {
                minTime = comp.cost.timeMs();
                minRows = comp.cost.rows();
            } else if (comp.comparison == Comparison.Less) {
                maxTime = comp.cost.timeMs();
                maxRows = comp.cost.rows();
            }
        }
        double timeMsSum = 0, rowsSum = 0, timeCount = 0, rowsCount = 0;
        for (final var comp : comparisons) {
            if (comp.comparison != Comparison.Similar) continue;
            if (minTime != null && comp.cost.timeMs() < minTime) continue;
            if (maxTime != null && comp.cost.timeMs() > maxTime) continue;
            timeMsSum += comp.cost.timeMs(); timeCount++;
        }
        for (final var comp : comparisons) {
            if (comp.comparison != Comparison.Similar) continue;
            if (minRows != null && comp.cost.rows() < minRows) continue;
            if (maxRows != null && comp.cost.rows() > maxRows) continue;
            rowsSum += comp.cost.rows(); rowsCount++;
        }

        Double totalTime;
        if (timeCount > 0)
            totalTime = timeMsSum / timeCount;
        else if (minTime != null && maxTime != null)
            totalTime = minTime + maxTime / 2;
        else if (minTime != null)
            totalTime = minTime;
        else totalTime = maxTime;

        Double totalRows;
        if (rowsCount > 0)
            totalRows = rowsSum / rowsCount;
        else if (minRows != null && maxRows != null)
            totalRows = minRows + maxRows / 2;
        else if (minRows != null)
            totalRows = minRows;
        else totalRows = maxRows;

        return new NodeCostData(totalRows, totalRows, totalTime);
    }

    private static enum Comparison {
        Less,
        Similar,
        More,
        Indeterminate;

        public Comparison add(Comparison c) {
            if (c == Indeterminate) return Indeterminate;
            return switch (this) {
                case Similar -> c;
                case Indeterminate -> Indeterminate;
                case Less -> (c == More) ? Indeterminate : Less;
                case More -> (c == Less) ? Indeterminate : More;
            };
        }
    }

    private static record ConcreteComparison(Comparison comparison, NodeEvalData cost) { }

    public Comparison compare(QueryNode qp1, QueryNode qp2) {
        if (!(qp1 instanceof DatasourceNode)) {
            return Comparison.Indeterminate;
        }
        // obtain filters from qp1, compare to qp2
        // you have a guaranteed same structure except for filters

        final var node1 = (DatasourceNode)qp1;
        final var node2 = (DatasourceNode)qp2;
        var returnVal = Comparison.Similar;

        node1.filters.sort((f1, f2) -> compareFilterParams(node1, f1, node1, f2));
        node2.filters.sort((f1, f2) -> compareFilterParams(node2, f1, node2, f2));

        // sort the node filters according to compareFilterParams()
        int i1 = 0, i2 = 0;
        while (i1 < node1.filters.size() && i2 < node2.filters.size()) {
            final var filter1 = node1.filters.get(i1);
            final var filter2 = node2.filters.get(i2);

            final int difference = compareFilterParams(node1, filter1, node2, filter2);
            if (difference < 0) {
                returnVal = returnVal.add(Comparison.Less);
                i1++; // filter1 is earlier (node2 definitely does not have it), we have to catch up to filter2
                break;
            } else if (difference > 0) {
                returnVal = returnVal.add(Comparison.More);
                i2++; // mirrored scenario as above
                break;
            } else {
                switch (filter1.operator) {
                case Equal:
                    // this assumes even distribution of value
                    returnVal = returnVal.add(Comparison.Similar);
                    break;
                case LessOrEqual:
                    // or greater, greaterorequal; depending on where the variable is, where the constant is, and whether the constant value is larger or lesser, returnVal.add(Less or More)

                    if (filter1.arguments.get(0) instanceof Variable && filter1.arguments.get(1) instanceof Constant) {
                        final var constA = (Constant)(filter1.arguments.get(1));
                        final var constB = (Constant)(filter1.arguments.get(1));

                        final var comparison = constA.value().compareTo(constB.value());
                        if (comparison < 0) returnVal = returnVal.add(Comparison.Less);
                        if (comparison > 0) returnVal = returnVal.add(Comparison.More);
                        else returnVal = returnVal.add(Comparison.Similar);
                    }
                    // TODO: other variants & variants with non-strings

                    returnVal = returnVal.add(Comparison.Indeterminate);
                    break;
                default:
                    return Comparison.Indeterminate;
                }
                i1++; i2++;
            }
        }
        if (i1 < node1.filters.size()) {
            returnVal = returnVal.add(Comparison.Less);
        } else if (i2 < node2.filters.size()) {
            returnVal = returnVal.add(Comparison.More);
        }
        return returnVal;
    }

    static int compareFilterParams(QueryNode node1, Computation filter1, QueryNode node2, Computation filter2) {
        if (filter1.operator != filter2.operator) {
            return filter1.operator.ordinal() - filter2.operator.ordinal();
        } else if (filter1.arguments.size() != filter2.arguments.size()) { // TODO: set operations should not be this strict
            return filter1.arguments.size() - filter2.arguments.size();
        }
        for (int i = 0; i < filter1.arguments.size(); i++) {
            // Expression can be: Constant, Variable, or a nested Computation, or let's say another unknown

            final Expression arg1 = filter1.arguments.get(i), arg2 = filter2.arguments.get(i);
            int order1 = 0, order2 = 0;

            if (arg1 instanceof Variable var1 && arg2 instanceof Variable var2) {
                // FIXME: this probably won't work when filtering through a non-returned column
                final var sig1 = node1.structure.tryFindDescendantByVariable(var1).getSignatureFromRoot();
                final var sig2 = node2.structure.tryFindDescendantByVariable(var2).getSignatureFromRoot();
                final var result = sig1.compareTo(sig2);
                if (result != 0) return result;
                continue;
            }

            if (arg1 instanceof Constant) order1 = 0;
            else if (arg1 instanceof Variable) order1 = 1;
            else if (arg1 instanceof Computation) order1 = 2;

            if (arg2 instanceof Constant) order2 = 0;
            else if (arg2 instanceof Variable) order2 = 1;
            else if (arg2 instanceof Computation) order2 = 2;

            if (order1 - order2 != 0) return order1 - order2;
        }
        return 0;
    }

    public static record CacheEntry(DatasourceNode node, DataModel dataModel) {}

    // This converter ignores all filters (they are to be compared manually)
    static class PlanToCacheKeyConverter implements QueryVisitor<Void> {

        final StringBuilder stringBuilder = new StringBuilder();

        private PlanToCacheKeyConverter() {}

        public static String run(QueryNode node) {
            final var converter = new PlanToCacheKeyConverter();
            node.accept(converter);
            return converter.stringBuilder.toString();
        }

        static String joinCandidateToString(JoinCandidate candidate) {

            StringBuilder sb = new StringBuilder();
            sb.append(candidate.from().kind.kindName());
            sb.append('[');
            sb.append(candidate.from().getPatternTree(candidate.variable()).computePathFromRoot());
            sb.append("] ");

            sb.append(candidate.type().name());
            sb.append("-JOIN ");

            sb.append(candidate.to().kind.kindName());
            sb.append('[');
            sb.append(candidate.to().getPatternTree(candidate.variable()).computePathFromRoot());
            sb.append(']');

            // TODO later: recursion, isOptional
            return sb.toString();
        }

        @Override public Void visit(DatasourceNode node) {
            stringBuilder.append("datasrc:");
            stringBuilder.append(node.datasource.identifier);
            stringBuilder.append("(");

            // It should work to only save the single kind or only joinCandidates, as they span all kinds
            if (node.joinCandidates.size() > 0) {
                final List<String> sortedJoinCandidates = node.joinCandidates.stream().map(jc -> joinCandidateToString(jc)).sorted().toList();
                for (final var joinCandidate : sortedJoinCandidates) {
                    stringBuilder.append(joinCandidate);
                    stringBuilder.append(',');
                }
            } else {
                final var onlyKind = node.kinds.iterator().next();
                stringBuilder.append(onlyKind.kind.kindName());
            }

            stringBuilder.append(")");
            // TODO: pattern for kinds (must be properly ordered)
            return null;
        }

        @Override public Void visit(FilterNode node) {
            node.child().accept(this);
            return null;
        }

        @Override public Void visit(JoinNode node) {
            final var candidate = node.candidate;

            stringBuilder.append('(');

            node.fromChild().accept(this);
            stringBuilder.append('[');
            stringBuilder.append(candidate.from().getPatternTree(candidate.variable()).computePathFromRoot());
            stringBuilder.append("] ");

            stringBuilder.append(candidate.type().name());
            stringBuilder.append("-JOIN ");

            node.toChild().accept(this);
            stringBuilder.append('[');
            stringBuilder.append(candidate.to().getPatternTree(candidate.variable()).computePathFromRoot());
            stringBuilder.append(']');

            stringBuilder.append(')');

            return null;
        }

        @Override public Void visit(MinusNode node) {
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

        @Override public Void visit(OptionalNode node) {
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

        @Override public Void visit(UnionNode node) {
            throw new UnsupportedOperationException("Unimplemented method 'visit'");
        }

    }
}
