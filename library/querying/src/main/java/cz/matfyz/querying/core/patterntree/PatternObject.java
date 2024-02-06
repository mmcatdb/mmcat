package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.utils.LineStringBuilder;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.querying.parsing.ParserNode.Term;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class represents a node in the pattern tree or an auxiliary node.
 * An auxiliary node is not part of the pattern (it is still part of the kind). It is needed for joining with other kind patterns.
 */
public class PatternObject {
    
    public final SchemaObject schemaObject;
    /**
     * If the term is null, the object is not part of the pattern. It is, however, still part of the kind.
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

    public PatternObject createChild(SchemaEdge schemaEdge, @Nullable WhereTriple triple) {
        final var edgeToChild = new EdgeData(schemaEdge, triple, this);

        final Term childTerm = triple == null
            ? null
            : schemaEdge.direction()
                ? triple.object
                : triple.subject;

        if (!(schemaEdge.signature() instanceof BaseSignature baseSignature))
            throw GeneralException.message("Non-base signature " + schemaEdge.signature() + " in pattern tree.");

        final var child = new PatternObject(schemaEdge.to(), childTerm, edgeToChild);
        children.put(baseSignature, child);

        return child;
    }

    public Collection<PatternObject> children() {
        return children.values();
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

    private void print(LineStringBuilder builder) {
        if (children.size() == 0) {
            builder.append("(").append(term == null ? "null" : term).append(")");
            return;
        }

        builder.append("{");
        builder
            .down().nextLine()
            .append("(").append(term == null ? "null" : term).append("),").nextLine();

        children.entrySet().forEach(child -> {
            builder.append(child.getKey()).append(": ");
            child.getValue().print(builder);
            builder.append(",").nextLine();
        });

        builder
            .remove().remove().up().nextLine()
            .append("}");
    }

    @Override
    public String toString() {
        final var builder = new LineStringBuilder(0);
        print(builder);
        return builder.toString();
    }

}
