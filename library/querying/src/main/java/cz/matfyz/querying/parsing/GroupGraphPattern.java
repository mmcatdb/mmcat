package cz.matfyz.querying.parsing;

import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.GraphUtils.TreeBuilder;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class GroupGraphPattern implements ParserNode {

    public final List<WhereTriple> triples;
    public final List<ConditionFilter> conditionFilters;
    public final List<ValueFilter> valueFilters;

    public final TermTree termTree;

    GroupGraphPattern(List<WhereTriple> triples, List<ConditionFilter> conditionFilters, List<ValueFilter> valueFilters) {
        this.triples = triples;
        this.conditionFilters = conditionFilters;
        this.valueFilters = valueFilters;

        this.termTree = GraphUtils.treeFromEdges(triples, new TermTreeBuilder());
    }

    public static class TermTree implements Tree<TermTree> {

        public final Term term;
        /** Is null if it's the root node. */
        @Nullable
        public final TermTree parent;
        public final List<TermTree> children = new ArrayList<>();

        TermTree(Term term, TermTree parent) {
            this.term = term;
            this.parent = parent;
        }

        TermTree createChild(Term term) {
            final var child = new TermTree(term, this);
            children.add(child);

            return child;
        }

        @Override public @Nullable TermTree parent() {
            return parent;
        }

        @Override public List<TermTree> children() {
            return children;
        }

        @Override public int compareTo(TermTree other) {
            return term.compareTo(other.term);
        }

    }

    private static class TermTreeBuilder implements TreeBuilder<Term, TermTree> {

        @Override public TermTree createRoot(Term payload) {
            return new TermTree(payload, null);
        }

        @Override public TermTree createChild(TermTree parent, Term payload) {
            return parent.createChild(payload);
        }

    }

}
