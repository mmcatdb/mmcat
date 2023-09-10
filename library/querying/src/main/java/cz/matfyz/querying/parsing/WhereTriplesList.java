package cz.matfyz.querying.parsing;

import java.util.List;

class WhereTriplesList extends ParserNode {

    @Override WhereTriplesList asWhereTriplesList() {
        return this;
    }

    public final List<WhereTriple> triples;

    public WhereTriplesList(List<WhereTriple> triples) {
        this.triples = triples;
    }

}