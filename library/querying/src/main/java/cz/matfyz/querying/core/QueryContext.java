package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.BaseControlWrapper.ControlWrapperProvider;
import cz.matfyz.core.querying.Variable;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class collects and provides global information about the whole query.
 */
public class QueryContext {

    // Extracting
    // There is exactly one schema object for each term. However, there might be multiple terms for each object.

    private final Map<Variable, SchemaObject> termToObject = new TreeMap<>();
    // private final Map<SchemaObject, List<Term>> objectToTerms = new TreeMap<>();

    public QueryContext addTerm(Variable term, SchemaObject object) {
        // FIXME

        termToObject.put(term, object);
        // objectToTerms.computeIfAbsent(object, k -> new ArrayList<>()).add(term);

        return this;
    }

    public SchemaObject getObjexForVariable(Variable term) {
        return termToObject.get(term);
    }

    // public List<Term> getTermsForObject(SchemaObject object) {
    //     return objectToTerms.get(object);
    // }

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
