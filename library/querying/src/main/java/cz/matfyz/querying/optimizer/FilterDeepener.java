package cz.matfyz.querying.optimizer;

import cz.matfyz.abstractwrappers.exception.QueryException;
import cz.matfyz.core.querying.Computation;
import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.core.querytree.JoinNode;
import cz.matfyz.querying.core.querytree.MinusNode;
import cz.matfyz.querying.core.querytree.OptionalNode;
import cz.matfyz.querying.core.querytree.QueryNode;
import cz.matfyz.querying.core.querytree.QueryVisitor;
import cz.matfyz.querying.core.querytree.UnionNode;
import cz.matfyz.querying.planner.QueryPlan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

class FilterDeepener implements QueryVisitor<Boolean> {

    public static void run(QueryPlan queryPlan) {
        final var filtersToProcess = findAllFilterNodes(queryPlan);

        for (var filterNode : filtersToProcess) {
            var deepener = new FilterDeepener(filterNode, queryPlan, filtersToProcess);

            boolean deepened = false;
            while (deepener.tryDeepen()) {
                deepened = true;
            }
            if (deepened && filterNode.child() != null) { // fix the filter's structure
                filterNode.structure = filterNode.child().structure.copy();
            }
        }
    }


    private static ArrayList<FilterNode> findAllFilterNodes(QueryPlan queryPlan) {
        final var filterNodes = new ArrayList<FilterNode>();

        GraphUtils.forEachDFS(queryPlan.root, node -> {
            if (node instanceof FilterNode f) filterNodes.add(f);
        });

        return filterNodes;
    }

    private static boolean structureCoversVariables(ResultStructure structure, Set<Variable> variables) {
        for (final var argVar : variables) {
            if (structure.tryFindDescendantByVariable(argVar) == null) return false;
        }
        return true;
    }

    private static Set<Variable> extractVariables(Computation computation) {
        final var vars = new HashSet<Variable>();
        extractVariables(computation, vars);
        return vars;
    }

    private static void extractVariables(Computation computation, HashSet<Variable> extractedVars) {
        for (final var arg : computation.arguments) {
            if (arg instanceof Variable var) {
                extractedVars.add(var);
            }
            else if (arg instanceof Computation comp) {
                extractVariables(comp, extractedVars);
            }
        }
    }


    final QueryPlan queryPlan;
    final FilterNode filterNode;
    final Set<Variable> filterVariables;
    final ArrayList<FilterNode> filtersToProcess; // If a filter is copied, it shall be added and processed later

    private FilterDeepener(FilterNode filterNode, QueryPlan queryPlan, ArrayList<FilterNode> filtersToProcess) {
        this.queryPlan = queryPlan;
        this.filterNode = filterNode;
        this.filterVariables = extractVariables(filterNode.filter);
        this.filtersToProcess = filtersToProcess;
    }

    boolean tryDeepen() {
        if (filterNode.child() == null) return false; // We're at the bottom

        final var parentBak = filterNode.parent();

        final boolean deepened = filterNode.child().accept(this);
        if (deepened) {
            // The visitor has set the filter's child as its parent, now just fix the parent references
            replaceParentsChild(parentBak, filterNode, filterNode.parent());
        }

        return deepened;
    }


    private void replaceParentsChild(@Nullable QueryNode parent, QueryNode originalChild, QueryNode replacementChild) {
        if (parent != null) {
            if (!parent.replaceChild(originalChild, replacementChild))
                throw QueryException.message("Inconsistent query tree structure");
        }
        else {
            replacementChild.setParent(null);
            queryPlan.root = replacementChild;
        }
    }

    @Override public Boolean visit(DatasourceNode childNode) {
        // TODO: Check filterNotIndexable (when it will matter, i.e. with Cassandra)

        final boolean canFilter = queryPlan.context.getProvider().getControlWrapper(childNode.datasource).getQueryWrapper().isFilterSupported(filterNode.filter.operator);
        if (!canFilter)
            return false;

        childNode.filters.add(filterNode.filter);

        // Signify to the algorithm that the filter is below the DatasourceNode and cannot be further deepened
        filterNode.setParent(childNode);
        filterNode.setChild(null);

        // The filterNode will be garbage collected afterwards
        return true;
    }

    @Override public Boolean visit(FilterNode childNode) {
        // It is automatically deepenable, no check needed

        replaceParentsChild(filterNode, childNode, childNode.child());
        replaceParentsChild(childNode, childNode.child(), filterNode);

        return true;
    }

    @Override public Boolean visit(JoinNode childNode) {
        // checks if one of the children contains all filter vars, if yes deepen only to that child;
        // if both children contain all filter vars, then without knowing how the results will be merged, we should propagate to both children

        final boolean coveredByFrom = structureCoversVariables(childNode.fromChild().structure, filterVariables);
        final boolean coveredByTo = structureCoversVariables(childNode.toChild().structure, filterVariables);

        if (coveredByFrom) {
            replaceParentsChild(filterNode, childNode, childNode.fromChild());
            replaceParentsChild(childNode, childNode.fromChild(), filterNode);
        }

        if (coveredByTo) {
            final FilterNode filterNode2;
            if (coveredByFrom) {
                filterNode2 = new FilterNode(childNode, filterNode.filter);
                filtersToProcess.add(filterNode2);
            }
            else {
                filterNode2 = filterNode;
            }

            replaceParentsChild(filterNode2, childNode, childNode.toChild());
            replaceParentsChild(childNode, childNode.toChild(), filterNode2);
        }

        return coveredByFrom || coveredByTo;
    }

    @Override public Boolean visit(MinusNode childNode) {
        // TODO: Here the filter needs to be spread to both children OR possibly just the primary one
        return false;
    }

    @Override public Boolean visit(OptionalNode childNode) {
        // TODO: Probably same as JoinNode?
        return false;
    }

    @Override public Boolean visit(UnionNode childNode) {
        // Here the filter needs to be spread to all children
        // TODO: Test these nodes once they are properly supported

        boolean first = true;

        for (final var coac : childNode.children()) {
            final FilterNode currentFilter = first ? filterNode : new FilterNode(childNode, filterNode.filter);

            if (first)
                first = false;
            else
                filtersToProcess.add(currentFilter);

            replaceParentsChild(currentFilter, childNode, coac);
            replaceParentsChild(childNode, coac, currentFilter);
        }

        return true;
    }

}
