package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.printable.*;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.querying.parsing.ParserNode.Term;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents a node in the PT (pattern tree) of a specific kind. It can be an auxiliary node.
 * An auxiliary node is not part of the QP (query pattern) (it is still part of the AP (access path) of the kind). It is needed for joining with PTs of other kinds.
 * On one hand, the the PT is a "subset" of AP, meaning that nodes from QP that aren't part of AP are not included.
 * On the other hand, it can contain more nodes. More specifically, AP can contain composite signatures while PT can't. Therefore, each edge from AP with a composite signature is mapped to multiple edges in PT.
 */
public class PatternObject implements Comparable<PatternObject>, Printable {

    public final SchemaObject schemaObject;
    /**
     * If the term is null, the object is not part of the query pattern. It is, however, still part of the kind.
     * The term can't be null if this object is the root of the pattern tree.
     */
    public final @Nullable Term term;
    /** If this property is null, the patternObject is the root of the pattern tree. */
    private final @Nullable EdgeData edgeFromParent;

    private final Map<BaseSignature, PatternObject> children = new TreeMap<>();

    private PatternObject(SchemaObject schemaObject, @Nullable Term term, @Nullable EdgeData edgeFromParent) {
        this.schemaObject = schemaObject;
        this.term = term;
        this.edgeFromParent = edgeFromParent;
    }

    public static PatternObject createRoot(SchemaObject schemaObject, Term term) {
        return new PatternObject(schemaObject, term, null);
    }

    public PatternObject getOrCreateChild(SchemaEdge schemaEdge, @Nullable WhereTriple triple) {
        if (!(schemaEdge.signature() instanceof BaseSignature baseSignature))
            throw GeneralException.message("Non-base signature " + schemaEdge.signature() + " in pattern tree.");

        final var currentChild = children.get(baseSignature);
        if (currentChild != null)
            return currentChild;

        final var edgeToChild = new EdgeData(schemaEdge, triple, this);
        final Term childTerm = triple == null
            ? null
            : schemaEdge.direction()
                ? triple.object
                : triple.subject;

        final var child = new PatternObject(schemaEdge.to(), childTerm, edgeToChild);
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
        // The triple might be oriendted differently than the schema edge - because all triples have to have positive signature, however edges allow duals.
        WhereTriple triple,
        PatternObject from
    ) {}

    @Override public void printTo(Printer printer) {
        printer.append("(").append(term == null ? "null" : term).append(")");
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
