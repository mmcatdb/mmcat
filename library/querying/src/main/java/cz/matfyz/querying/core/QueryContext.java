package cz.matfyz.querying.core;

import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.parsing.Variable;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class collects and provides global information about the whole query.
 */
public class QueryContext {

    // TODO this class is not needed now - decide if it should be removed or not

    // Parsing

    private final Map<BaseSignature, WhereTriple> triples = new TreeMap<>();

    public QueryContext addTriple(WhereTriple triple) {
        this.triples.put(triple.signature, triple);

        return this;
    }

    public WhereTriple getTriple(BaseSignature signature) {
        return triples.get(signature);
    }

    // Extracting

    private final Map<String, SchemaObject> objects = new TreeMap<>();

    public QueryContext defineVariable(Variable variable, SchemaObject object) {
        objects.put(variable.getIdentifier(), object);

        return this;
    }

    public SchemaObject getObject(Variable variable) {
        return objects.get(variable.getIdentifier());
    }

}
