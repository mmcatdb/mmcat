package cz.matfyz.querying.parsing;

import java.util.List;

public class SelectClause implements ParserNode {

    public final List<SelectTriple> triples;

    public SelectClause(List<SelectTriple> triples) {
        this.triples = triples;
    }

}
