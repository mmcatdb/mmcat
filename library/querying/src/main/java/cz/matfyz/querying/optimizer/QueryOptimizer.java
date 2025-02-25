package cz.matfyz.querying.optimizer;

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

import javax.annotation.Nullable;

public class QueryOptimizer {

    public static QueryPlan run(QueryPlan plan) {
        return new QueryOptimizer(plan).run();
    }

    private final QueryPlan original;

    private QueryOptimizer(QueryPlan original) {
        this.original = original;
    }

    private QueryPlan run() {

        deepenFilters();

        // TODO Other optimization techniques:
        // - Split filters to further deepen them
        // - Merge consecutive filters to reduce filter passes
        // - Possibly merge filters into joins directly below them? (requires a concept of theta-join)

        return original;
    }

    private void deepenFilters() {
        for (var filterNode : findAllFilterNodes()) {
            boolean deepened = false;
            while (tryDeepen(filterNode)) {
                deepened = true;
            }
            if (deepened && filterNode.child() != null) { // fix filter structure
                filterNode.structure = filterNode.child().structure.copy();
            }
        }
    }

    private ArrayList<FilterNode> findAllFilterNodes() {
        final var filterNodes = new ArrayList<FilterNode>();

        GraphUtils.forEachDFS(original.root, node -> {
            if (node instanceof FilterNode f) filterNodes.add(f);
        });

        return filterNodes;
    }

    private static void replaceParentsChild(@Nullable QueryNode parent, QueryNode originalChild, QueryNode replacementChild) {
        if (parent != null) {
            parent.children().replaceAll(node -> { return node == originalChild ? replacementChild : node; });
        }
        replacementChild.setParent(parent);
    }

    boolean tryDeepen(FilterNode filterNode) {
        if (filterNode.child() == null) return false;

        final var parentBak = filterNode.parent();

        final boolean deepened = filterNode.child().accept(new FilterDeepener(filterNode));
        if (deepened) {
            if (parentBak == null) {
                original.root = filterNode.parent();
            } else {
                replaceParentsChild(parentBak, filterNode, filterNode.parent());
            }
        }

        return deepened;
    }


    static class FilterDeepener implements QueryVisitor<Boolean> {

        final FilterNode filterNode;
        final Set<Variable> filterVariables;

        private FilterDeepener(FilterNode filterNode) {
            this.filterNode = filterNode;
            this.filterVariables = extractVariables(filterNode.filter);
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

        @Override
        public Boolean visit(DatasourceNode childNode) {
            // TODO: Need to check whether filters are supported, but that is only available through wrappers and not datasources; for now we assume YES

            childNode.filters.add(filterNode.filter);
            // To enforce post-deepening invariants (no other reason), we need to assign the DatasourceNode as a parent to the filter
            filterNode.setParent(childNode);
            filterNode.setChild(null);

            // The filterNode will be garbage collected afterwards

            return true;
        }

        @Override
        public Boolean visit(FilterNode childNode) {
            // It is automatically deepenable, no check needed

            replaceParentsChild(filterNode, childNode, childNode.child());
            replaceParentsChild(childNode, childNode.child(), filterNode);

            return true;
        }

        @Override
        public Boolean visit(JoinNode childNode) {
            // checks if one of the children contains all filter vars, if yes deepen only to that child;
            // if both children contain all filter vars... I guess we can use both, but that probably means the filter is mentally challenged anyway

            if (structureCoversVariables(childNode.fromChild().structure, filterVariables)) {
                replaceParentsChild(filterNode, childNode, childNode.fromChild());
                replaceParentsChild(childNode, childNode.fromChild(), filterNode);
                return true;
            } else if (structureCoversVariables(childNode.toChild().structure, filterVariables)) {
                replaceParentsChild(filterNode, childNode, childNode.toChild());
                replaceParentsChild(childNode, childNode.toChild(), filterNode);
                return true;
            }

            return false;
        }

        @Override
        public Boolean visit(MinusNode childNode) {
            // TODO: here the filter needs to be spread to both children OR possibly just the primary one
            return false;
        }

        @Override
        public Boolean visit(OptionalNode childNode) {
            // TODO: I'm not actually sure here
            return false;
        }

        @Override
        public Boolean visit(UnionNode childNode) {
            // TODO: here the filter needs to be spread to both children
            return false;
        }

    }

}
