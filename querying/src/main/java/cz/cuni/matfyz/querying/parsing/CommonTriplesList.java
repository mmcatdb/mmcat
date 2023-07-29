package cz.cuni.matfyz.querying.parsing;

import java.util.List;

class CommonTriplesList extends QueryNode {

    @Override CommonTriplesList asCommonTriplesList() {
        return this;
    }

    final List<CommonTriple> triples;

    CommonTriplesList(List<CommonTriple> triples) {
        this.triples = triples;
    }

}