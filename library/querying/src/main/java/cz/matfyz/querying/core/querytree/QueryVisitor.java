package cz.matfyz.querying.core.querytree;

public interface QueryVisitor {
    
    void visit(DatabaseNode node);
    void visit(FilterNode node);
    void visit(JoinNode node);
    void visit(MinusNode node);
    void visit(OptionalNode node);
    void visit(PatternNode node);
    void visit(UnionNode node);

}
