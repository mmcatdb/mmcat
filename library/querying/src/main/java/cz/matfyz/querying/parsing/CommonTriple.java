package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.ParserNode.Term;

class CommonTriple implements Statement {

    final Variable subject;
    final String predicate;
    final Term object;

    CommonTriple(Variable subject, String predicate, Term object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

}
