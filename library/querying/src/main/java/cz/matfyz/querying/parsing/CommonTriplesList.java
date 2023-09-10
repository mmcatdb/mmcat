package cz.matfyz.querying.parsing;

import java.util.List;

class CommonTriplesList extends ParserNode {

    @Override CommonTriplesList asCommonTriplesList() {
        return this;
    }

    final List<CommonTriple> triples;

    CommonTriplesList(List<CommonTriple> triples) {
        this.triples = triples;
    }

}