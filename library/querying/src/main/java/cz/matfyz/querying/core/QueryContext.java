package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.parsing.Term;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class collects and provides global information about the whole query.
 */
public class QueryContext {

    // Extracting

    public record SchemaObjectContext(SchemaObject object, ArrayList<Mapping> mappings, ArrayList<Signature> signatures) { }

    private final Map<Term, SchemaObjectContext> termToObject = new TreeMap<>();
    private final Map<SchemaObject, Term> objectToTerm = new TreeMap<>();

    public QueryContext addTerm(Term term, SchemaObject object) {
        termToObject.put(term, new SchemaObjectContext(object, new ArrayList<>(), new ArrayList<>()));
        objectToTerm.put(object, term);

        return this;
    }

    public QueryContext addOrUpdateTerm(Term term, SchemaObject object, Mapping kind, Signature signature) {
        var existing = termToObject.get(term);

        if(existing == null) {
            existing = new SchemaObjectContext(object, new ArrayList<>(), new ArrayList<>());

            termToObject.put(term, existing);
            objectToTerm.put(object, term);
        }

        existing.mappings.add(kind);
        existing.signatures.add(signature);

        return this;
    }

    public SchemaObject getObject(Term term) {
        return termToObject.get(term).object;
    }

    public Term getTerm(SchemaObject object) {
        return objectToTerm.get(object);
    }

    public SchemaObjectContext getContext(Term term) {
        return termToObject.get(term);
    }

    // Schema category

    // TODO - should be unique per nested clause. However, the variables probably should be as well, so ...

    private @Nullable SchemaCategory schema;

    public @Nullable SchemaCategory getSchema() {
        return schema;
    }

    public QueryContext setSchema(SchemaCategory schema) {
        this.schema = schema;

        return this;
    }

    // Querying

    private @Nullable ControlWrapperProvider provider;

    public @Nullable ControlWrapperProvider getProvider() {
        return provider;
    }

    public QueryContext setProvider(ControlWrapperProvider provider) {
        this.provider = provider;

        return this;
    }

}
