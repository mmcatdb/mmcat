package cz.matfyz.querying.core;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.parsing.WhereTriple;
import cz.matfyz.querying.parsing.ParserNode.Term;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class collects and provides global information about the whole query.
 */
public class QueryContext {

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
    
    private final Map<Term, SchemaObject> termToObject = new TreeMap<>();
    
    public QueryContext addTerm(Term term, SchemaObject object) {
        termToObject.put(term, object);
        
        return this;
    }
    
    public SchemaObject getObject(Term term) {
        return termToObject.get(term);
    }

    // Schema category

    // TODO - should be unique per nested clause. However, the variables probably should be as well, so ...

    private SchemaCategory schema;

    public SchemaCategory getSchema() {
        return schema;
    }

    public QueryContext setSchema(SchemaCategory schema) {
        this.schema = schema;

        return this;
    }    

}
