package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.printable.*;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.querying.exception.GeneralException;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

// TODO This should probably be reworked. It doesn't really make sense to break down composite signatures into base signatures, because the if an object isn't covered by a mapping, but it's in a middle of a composite morphism, it can't be extracted from the database. It needs to be filtered down the line, anyway.

/**
 * This class represents a node in the PT (pattern tree) of a specific kind.
 * On one hand, the PT is a "subset" of AP (access path), meaning that nodes from QP (query pattern) that aren't part of AP are not included.
 * On the other hand, it can contain more nodes. More specifically, AP can contain composite signatures while PT can't. Therefore, each edge from AP with a composite signature is mapped to multiple edges in PT.
 */
public class PatternTree implements Comparable<PatternTree>, Printable {

    public final Variable variable;

    public final SchemaObject schemaObject;
    /** This property is null if and only if this object is the root. */
    private final @Nullable PatternTree parent;
    /** This property is null if and only if this object is the root. */
    private final @Nullable SchemaEdge edgeFromParent;

    private final Map<BaseSignature, PatternTree> children = new TreeMap<>();

    private PatternTree(SchemaObject schemaObject, Variable variable, @Nullable PatternTree parent, @Nullable SchemaEdge edgeFromParent) {
        this.schemaObject = schemaObject;
        this.variable = variable;
        this.parent = parent;
        this.edgeFromParent = edgeFromParent;
    }

    public static PatternTree createRoot(SchemaObject schemaObject, Variable variable) {
        return new PatternTree(schemaObject, variable, null, null);
    }

    public PatternTree getOrCreateChild(SchemaEdge schemaEdge, Variable variable) {
        if (!(schemaEdge.signature() instanceof BaseSignature baseSignature))
            throw GeneralException.message("Non-base signature " + schemaEdge.signature() + " in pattern tree.");

        final var currentChild = children.get(baseSignature);
        if (currentChild != null)
            return currentChild;

        final var child = new PatternTree(schemaEdge.to(), variable, this, schemaEdge);
        children.put(baseSignature, child);

        return child;
    }

    public Collection<PatternTree> children() {
        return children.values();
    }

    public boolean isChildOfArray() {
        return edgeFromParent != null
            && edgeFromParent.isArray();
    }

    @Nullable
    public BaseSignature signatureFromParent() {
        // The signature must be base because it comes from the SelectionTriple.
        return edgeFromParent != null
            ? (BaseSignature) edgeFromParent.signature()
            : null;
    }

    public Signature computePathFromRoot() {
        return edgeFromParent != null
            ? parent.computePathFromRoot().concatenate(edgeFromParent.signature())
            : Signature.createEmpty();
    }

    public boolean isTerminal() {
        return children.isEmpty();
    }

    @Override public void printTo(Printer printer) {
        printer.append("(").append(variable).append(")");
        if (children.size() == 0)
            return;

        printer
            .append(" {")
            .down().nextLine();

        children.entrySet().forEach(child -> {
            printer.append(child.getKey()).append(": ");
            printer.append(child.getValue());
            printer.append(",").nextLine();
        });

        printer
            .remove().up().nextLine()
            .append("}");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    @Override public int compareTo(PatternTree other) {
        return schemaObject.compareTo(other.schemaObject);
    }

    public record SerializedPatternTree(
        int objexKey,
        String term,
        Map<BaseSignature, SerializedPatternTree> children
    ) {};

    public SerializedPatternTree serialize() {
        final var map = new TreeMap<BaseSignature, SerializedPatternTree>();
        children.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue().serialize()));

        return new SerializedPatternTree(schemaObject.key().getValue(), variable.toString(), map);
    }

}
