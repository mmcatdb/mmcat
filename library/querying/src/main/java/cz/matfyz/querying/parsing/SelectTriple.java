package cz.matfyz.querying.parsing;

import cz.matfyz.querying.parsing.ParserNode.Term;

public class SelectTriple implements Statement {

    public final Variable subject;
    public final String name;
    // This has to be either Variable or Aggregation.
    public final Term object;

    SelectTriple(Variable subject, String name, Term object) {
        this.subject = subject;
        this.name = name;
        this.object = object;
    }

    static SelectTriple fromCommonTriple(CommonTriple common) {
        return new SelectTriple(common.subject, common.predicate, common.object);
    }

    @Override public String toString() {
        return subject.toString() + " " + name + " " + object.toString();
    }

}
