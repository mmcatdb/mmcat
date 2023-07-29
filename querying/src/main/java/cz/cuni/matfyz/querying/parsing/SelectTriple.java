package cz.cuni.matfyz.querying.parsing;

public class SelectTriple implements Statement {

    public final Variable subject;
    public final String name;
    public final ValueNode object;
    
    public SelectTriple(Variable subject, String name, ValueNode object) {
        this.subject = subject;
        this.name = name;
        this.object = object;
    }

    static SelectTriple fromCommonTriple(CommonTriple common) {
        return new SelectTriple(common.subject, common.predicate, common.object);
    }

}