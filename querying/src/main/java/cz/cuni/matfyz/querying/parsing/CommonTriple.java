package cz.cuni.matfyz.querying.parsing;

class CommonTriple implements Statement {

    final Variable subject;
    final String predicate;
    final ValueNode object;
    
    CommonTriple(Variable subject, String predicate, ValueNode object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

}