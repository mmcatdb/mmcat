package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.printable.*;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.querying.parsing.Term;
import cz.matfyz.querying.exception.GeneralException;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents a node in the PT (pattern tree) of a specific kind.
 * On one hand, the PT is a "subset" of AP (access path), meaning that nodes from QP (query pattern) that aren't part of AP are not included.
 * On the other hand, it can contain more nodes. More specifically, AP can contain composite signatures while PT can't. Therefore, each edge from AP with a composite signature is mapped to multiple edges in PT.
 */
public class PatternObject implements Comparable<PatternObject>, Printable {

    public final SchemaObject schemaObject;
    public final Term term;
    /** If this property is null, the PatternObject is the root of the pattern tree. */
    private final @Nullable EdgeData edgeFromParent;

    private final Map<BaseSignature, PatternObject> children = new TreeMap<>();

    private PatternObject(SchemaObject schemaObject, Term term, @Nullable EdgeData edgeFromParent) {
        this.schemaObject = schemaObject;
        this.term = term;
        this.edgeFromParent = edgeFromParent;
    }

    public static PatternObject createRoot(SchemaObject schemaObject, Term term) {
        return new PatternObject(schemaObject, term, null);
    }

    public PatternObject getOrCreateChild(SchemaEdge schemaEdge, Term term) {
        if (!(schemaEdge.signature() instanceof BaseSignature baseSignature))
            throw GeneralException.message("Non-base signature " + schemaEdge.signature() + " in pattern tree.");

        final var currentChild = children.get(baseSignature);
        if (currentChild != null)
            return currentChild;

        final var edgeToChild = new EdgeData(schemaEdge, this);
        final var child = new PatternObject(schemaEdge.to(), term, edgeToChild);
        children.put(baseSignature, child);

        return child;
    }

    public Collection<PatternObject> children() {
        return children.values();
    }

    public @Nullable PatternObject parent() {
        return edgeFromParent != null
            ? edgeFromParent.from
            : null;
    }

    public boolean isChildOfArray() {
        return edgeFromParent != null
            && edgeFromParent.schemaEdge.isArray();
    }

    @Nullable
    public BaseSignature signatureFromParent() {
        // The signature must be base because it comes from the WhereTriple.
        return edgeFromParent != null
            ? (BaseSignature) edgeFromParent.schemaEdge.signature()
            : null;
    }

    public Signature computePathFromRoot() {
        return edgeFromParent != null
            ? edgeFromParent.from.computePathFromRoot().concatenate(edgeFromParent.schemaEdge.signature())
            : Signature.createEmpty();
    }

    public boolean isTerminal() {
        return children.isEmpty();
    }

    private record EdgeData(
        SchemaEdge schemaEdge,
        PatternObject from
    ) {}

    @Override public void printTo(Printer printer) {
        printer.append("(").append(term).append(")");
        if (children.size() == 0)
            return;

        printer
            .append(" {")
            .down().nextLine();

        children.entrySet().forEach(child -> {
            printer.append(child.getKey()).append(": ");
            printer.append(child);
            printer.append(",").nextLine();
        });

        printer
            .remove().up().nextLine()
            .append("}");
    }

    @Override public String toString() {
        return Printer.print(this);
    }

    @Override public int compareTo(PatternObject other) {
        return schemaObject.compareTo(other.schemaObject);
    }

}
