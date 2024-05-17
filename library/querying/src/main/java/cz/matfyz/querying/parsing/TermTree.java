package cz.matfyz.querying.parsing;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.querying.parsing.Term.Variable;
import cz.matfyz.core.utils.printable.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

public class TermTree<TEdge> implements ParserNode, Tree<TermTree<TEdge>>, Printable {
    /** If this is root or inner node, this property has to be Variable. */
    public final Term term;
    public final List<TermTree<TEdge>> children = new ArrayList<>();
    /** This property is null if and only if this object is the root. */
    private @Nullable TermTree<TEdge> parent;
    /** This property is null if and only if this object is the root. */
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

    public static <SEdge> TermTree<SEdge> root(Variable term) {
        return new TermTree<>(term, null);
    }

    public static <SEdge> TermTree<SEdge> child(Term term, SEdge edge) {
        return new TermTree<>(term, edge);
    }

    public static <SEdge> TermTree<SEdge> fromList(List<TermTree<SEdge>> termTrees) {
        // We create a map of each term (that is used in a leaf) to the corresponding leaf. Each term should be ofc in at most one leaf.
        final Map<Term, TermTree<SEdge>> termToLeaf = new TreeMap<>();
        for (TermTree<SEdge> termTree : termTrees)
            addLeafsToMap(termTree, termToLeaf);

        @Nullable TermTree<SEdge> root = null;
        for (TermTree<SEdge> termTree : termTrees) {
            final Term term = termTree.term;
            final TermTree<SEdge> leaf = termToLeaf.get(term);
            // If the term doesn't have a corresponding leaf, it means it's a root.
            if (leaf == null) {
                // TODO - check if there isn't another root already.
                root = termTree;
                continue;
            }

            // Otherwise, we just add all its children to the leaf.
            termTree.children.forEach(leaf::addChild);
        }

        // TODO - check if there is a root.
        return root;
    }

    private static <SEdge> void addLeafsToMap(TermTree<SEdge> termTree, Map<Term, TermTree<SEdge>> termToLeaf) {
        if (termTree.children.isEmpty()) {
            termToLeaf.put(termTree.term, termTree);
            return;
        }

        for (TermTree<SEdge> child : termTree.children)
            addLeafsToMap(child, termToLeaf);
    }

    public interface TripleCreator<SEdge, TTriple> {
        TTriple create(Variable parent, SEdge edge, Term child);
    }

    public <TTriple> List<TTriple> toTriples(TripleCreator<TEdge, TTriple> creator) {
        final List<TTriple> triples = new ArrayList<>();
        for (final TermTree<TEdge> child : children)
            addTriplesToList(child, term.asVariable(), triples, creator);

        return triples;
    }

    private static <TTriple, SEdge> void addTriplesToList(TermTree<SEdge> termTree, Variable parent, List<TTriple> triples, TripleCreator<SEdge, TTriple> creator) {
        final TTriple triple = creator.create(parent, termTree.edgeFromParent, termTree.term);
        triples.add(triple);

        for (final TermTree<SEdge> child : termTree.children)
            addTriplesToList(child, termTree.term.asVariable(), triples, creator);
    }

    @Override public void printTo(Printer printer) {
        if (edgeFromParent != null) {
            final Object edgeString = edgeFromParent instanceof Signature signature
                ? signature.toString(QueryVisitor.SIGNATURE_SEPARATOR)
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

}
