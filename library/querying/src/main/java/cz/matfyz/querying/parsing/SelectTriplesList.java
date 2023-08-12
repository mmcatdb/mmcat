package cz.matfyz.querying.parsing;

import java.util.List;

class SelectTriplesList extends QueryNode {

    @Override SelectTriplesList asSelectTriplesList() {
        return this;
    }

    public final List<SelectTriple> triples;

    public SelectTriplesList(List<SelectTriple> triples) {
        this.triples = triples;
    }

}