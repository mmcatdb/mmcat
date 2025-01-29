package cz.matfyz.querying.normalizer;

import cz.matfyz.core.querying.Expression;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.utils.printable.*;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A tree where all parent nodes are query variables. Leaves are expressions.
 * The edges are string names.
 */
public class ExpressionTree implements Printable {

    public final Expression expression;

    public Variable asVariable() {
        if (expression instanceof Variable variable)
            return variable;

        throw new IllegalStateException("This expression is not a variable: " + expression);
    }

    private final Map<String, ExpressionTree> children = new TreeMap<>();
    /** This property is null if and only if this object is the root. */
    private @Nullable ExpressionTree parent;
    /** This property is null if and only if this object is the root. */
    public final @Nullable String edgeFromParent;

    public @Nullable ExpressionTree getChild(String edge) {
        return children.get(edge);
    }

    public Collection<ExpressionTree> children() {
        return children.values();
    }

    private ExpressionTree(Expression expression, @Nullable String edgeFromParent) {
        this.expression = expression;
        this.edgeFromParent = edgeFromParent;
    }

    public static ExpressionTree createRoot(Variable variable) {
        return new ExpressionTree(variable, null);
    }

    public ExpressionTree createChild(Expression expression, String edge) {
        if (children.containsKey(edge))
            throw new IllegalStateException("Child with edge \"" + edge + "\" already exists.");

        final var child = new ExpressionTree(expression, edge);
        child.parent = this;

        children.put(edge, child);

        return child;
    }

    @Override public void printTo(Printer printer) {
        if (edgeFromParent != null)
            printer.append(edgeFromParent).append(" ");

        printer.append(expression);

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
