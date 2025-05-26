package cz.matfyz.querying.parser;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.printable.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.checkerframework.checker.nullness.qual.Nullable;

public class TermTree<TEdge> implements ParserNode, Tree<TermTree<TEdge>>, Printable {
    /** If this is root or inner node, this property has to be a Variable. */
    public final Term term;
    public final List<TermTree<TEdge>> children = new ArrayList<>();
    /** This property is null if and only if this node is the root. */
    private @Nullable TermTree<TEdge> parent;
    /** This property is null if and only if this node is the root. */
    public final @Nullable TEdge edgeFromParent;

    @Override public List<TermTree<TEdge>> children() {
        return children;
    }

    @Override public @Nullable TermTree<TEdge> parent() {
        return parent;
    }

    @Override public int compareTo(TermTree<TEdge> other) {
        return term.compareTo(other.term);
    }

    public void addChild(TermTree<TEdge> child) {
        this.children.add(child);
        child.parent = this;
    }

    private TermTree(Term term, @Nullable TEdge edgeFromParent) {
        this.term = term;
        this.edgeFromParent = edgeFromParent;
    }

    public static <SEdge> TermTree<SEdge> createRoot(Term term) {
        return new TermTree<>(term, null);
    }

    public static <SEdge> TermTree<SEdge> createChild(Term term, SEdge edge) {
        return new TermTree<>(term, edge);
    }

    public static <SEdge> TermTree<SEdge> createFromList(List<TermTree<SEdge>> termTrees) {
        return TreeMerger.merge(termTrees);
    }

    @Override public void printTo(Printer printer) {
        if (edgeFromParent != null) {
            final Object edgeString = edgeFromParent instanceof Signature signature
                ? signature.toString(AstVisitor.SIGNATURE_SEPARATOR)
                : edgeFromParent;
            printer.append(edgeString).append(" ");
        }

        printer.append(term);

        if (children.isEmpty())
            return;

        if (children.size() > 1)
            printer.down().nextLine();
        else
            printer.append(" ");

        for (int i = 0; i < children.size() - 1; i++) {
            final var child = children.get(i);
            printer.append(child);
            if (child.children.isEmpty())
                printer.append(" ;");
            printer.nextLine();
        }

        final var child = children.getLast();
        printer.append(child);
        if (child.children.isEmpty())
            printer.append(" .");
        printer.nextLine();

        if (children.size() > 1)
            printer.remove().up();
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    private static class TreeMerger<TEdge> {

        static <SEdge> TermTree<SEdge> merge(List<TermTree<SEdge>> termTrees) {
            return new TreeMerger<SEdge>().innerMerge(termTrees);
        }

        private TreeMerger() {}

        // We need to be able to merge the trees into a single tree.
        // So, we have the following limitations:
        //  1. Each term that is a root of a tree can be used at most once as a non-root term.
        //  2. There is exactly one root term that is never used as a non-root term (the global root). All other root terms are used exactly once as non-root terms.
        //  3. Root term can't be used as a non-root term of the same tree.

        private final Set<Variable> rootVariables = new TreeSet<>();
        private final Map<Variable, TermTree<TEdge>> variableToNode = new TreeMap<>();

        private Variable rootVariable;

        private TermTree<TEdge> innerMerge(List<TermTree<TEdge>> termTrees) {
            // First, we collect all root variables. Then we try to find them among the non-root terms (and check the above-mentioned limitations).
            termTrees.forEach(tree -> rootVariables.add(tree.term.asVariable()));

            for (TermTree<TEdge> termTree : termTrees) {
                rootVariable = termTree.term.asVariable();
                addChildrenToMap(termTree);
            }

            @Nullable TermTree<TEdge> root = null;

            for (TermTree<TEdge> termTree : termTrees) {
                final TermTree<TEdge> node = variableToNode.get(termTree.term.asVariable());
                // If the term doesn't have a corresponding inner node, it means it's the global root.
                if (node == null) {
                    if (root == null) {
                        root = termTree;
                        continue;
                    }

                    if (root.term.asVariable().equals(termTree.term.asVariable())) {
                        // There are multiple roots, but they correspond to the same variable, so we can just merge them.
                        termTree.children.forEach(root::addChild);
                        continue;
                    }

                    if (root != null)
                        // The 2. limitation is violated.
                        throw new IllegalArgumentException("There are multiple roots in the input trees.");
                }

                // Otherwise, we just merge it with the inner node.
                termTree.children.forEach(node::addChild);
            }

            if (root == null)
                // The 2. limitation is violated.
                throw new IllegalArgumentException("There is no root in the input trees.");

            return root;
        }

        private void addChildrenToMap(TermTree<TEdge> parent) {
            for (TermTree<TEdge> child : parent.children) {
                if (!child.term.isVariable())
                    // Again, only variables can be used as inner nodes, so we don't care about others.
                    continue;

                if (rootVariables.contains(child.term.asVariable())) {
                    final var variable = child.term.asVariable();

                    if (variableToNode.containsKey(variable))
                        // The 1. limitation is violated.
                        throw new IllegalArgumentException("The variable " + variable + " is used in multiple non-root nodes.");

                    if (rootVariable.equals(variable))
                        // The 3. limitation is violated.
                        throw new IllegalArgumentException("The variable " + variable + " is used as a non-root node of the same tree.");

                    variableToNode.put(variable, child);
                }

                addChildrenToMap(child);
            }
        }

    }

}
