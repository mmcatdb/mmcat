package cz.matfyz.querying.core.patterntree;

import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaCategory.SchemaEdge;
import cz.matfyz.querying.parsing.ParserNode.Term;
import cz.matfyz.querying.exception.GeneralException;
import cz.matfyz.querying.exception.QueryingException;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

public class PatternObject {
    
    private final SchemaObject schemaObject;
    private final Term term;
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

    public PatternObject createChild(SchemaEdge schemaEdge, @Nullable WhereTriple triple) {
        final var edgeToChild = new EdgeData(schemaEdge, triple);

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

    public boolean isTerminal() {
        return children.isEmpty();
    }

    private static record EdgeData(
        SchemaEdge schemaEdge,
        // The triple might be oriendted differently than the schema edge - because all triples have to have positive signature, however edges allow duals.
        @Nullable WhereTriple triple
    ) {}

}
