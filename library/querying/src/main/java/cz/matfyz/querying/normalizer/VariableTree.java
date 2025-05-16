package cz.matfyz.querying.normalizer;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.GraphUtils.Tree;
import cz.matfyz.core.utils.printable.*;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A tree where all nodes are query variables.
 * The edges represent base signatures of morphisms between the corresponding objexes.
 */
public class VariableTree implements Tree<VariableTree>, Printable {

    public final Variable variable;

    private final Map<BaseSignature, VariableTree> children = new TreeMap<>();
    /** This property is null if and only if this object is the root. */
    private @Nullable VariableTree parent;
    /** This property is null if and only if this object is the root. */
    public final @Nullable BaseSignature edgeFromParent;

    public @Nullable VariableTree getChild(BaseSignature edge) {
        return children.get(edge);
    }

    @Override public Collection<VariableTree> children() {
        return children.values();
    }

    @Override public @Nullable VariableTree parent() {
        return parent;
    }

    @Override public int compareTo(VariableTree other) {
        return variable.compareTo(other.variable);
    }

    private VariableTree(Variable variable, @Nullable BaseSignature edgeFromParent) {
        this.variable = variable;
        this.edgeFromParent = edgeFromParent;
    }

    public static VariableTree createRoot(Variable variable) {
        return new VariableTree(variable, null);
    }

    public VariableTree getOrCreateChild(Variable variable, BaseSignature edge) {
        final var current = children.get(edge);
        if (current != null)
            return current;

        final var child = new VariableTree(variable, edge);
        child.parent = this;
        children.put(edge, child);

        return child;
    }

    // TODO Create some general "print tree" method for this.
    @Override public void printTo(Printer printer) {
        if (edgeFromParent != null)
            printer.append(edgeFromParent).append(" ");

        printer.append(variable);

        if (children.isEmpty())
            return;


        if (children.size() > 1)
            printer.down().nextLine();
        else
            printer.append(" ");

        final var childrenValues = children.values().stream().toList();

        for (int i = 0; i < childrenValues.size() - 1; i++) {
            final var child = childrenValues.get(i);
            printer.append(child);
            if (child.children.isEmpty())
                printer.append(" ;");
            printer.nextLine();
        }

        final var child = childrenValues.getLast();
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
