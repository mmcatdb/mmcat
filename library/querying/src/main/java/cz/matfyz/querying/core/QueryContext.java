package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.parsing.Term;

import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class collects and provides global information about the whole query.
 */
public class QueryContext {

    // Extracting

    private final Map<Term, SchemaObject> termToObject = new TreeMap<>();
    private final Map<SchemaObject, Term> objectToTerm = new TreeMap<>();

    public QueryContext addTerm(Term term, SchemaObject object) {
        termToObject.put(term, object);
        objectToTerm.put(object, term);

        return this;
    }

    public SchemaObject getObject(Term term) {
        return termToObject.get(term);
    }

    public Term getTerm(SchemaObject object) {
        return objectToTerm.get(object);
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
