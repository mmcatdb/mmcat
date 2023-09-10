package cz.matfyz.querying.core;

import cz.matfyz.abstractwrappers.database.Kind;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.querying.parsing.WhereTriple;

import java.util.List;
import java.util.Set;

/**
 * This class represents a set of triples from one group. It has its own schema category and it is covered by a set of kinds.
 * When translating from the query, each clause (MINUS, UNION) result in its own pattern. However, the content of the OPTIONAL clause is included in the group pattern.
 */
public class Pattern {

    public final List<WhereTriple> triples;
    public final SchemaCategory schema;
    public final Set<Kind> kinds;

    public Pattern(List<WhereTriple> triples, SchemaCategory schema, Set<Kind> kinds) {
        this.triples = triples;
        this.schema = schema;
        this.kinds = kinds;
    }

}
