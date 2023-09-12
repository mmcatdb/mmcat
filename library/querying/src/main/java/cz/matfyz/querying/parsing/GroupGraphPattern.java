package cz.matfyz.querying.parsing;

import java.util.List;

public class GroupGraphPattern extends ParserNode {

    @Override GroupGraphPattern asGroupGraphPattern() {
        return this;
    }

    public final List<WhereTriple> triples;
    public final List<Filter> filters;
    public final List<Values> values;

    public GroupGraphPattern(List<WhereTriple> triples, List<Filter> filters, List<Values> values) {
        this.triples = triples;
        this.filters = filters;
        this.values = values;
    }

}