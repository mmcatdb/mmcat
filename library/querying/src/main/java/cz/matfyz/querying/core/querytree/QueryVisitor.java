package cz.matfyz.querying.core.querytree;

public interface QueryVisitor {
    
    default void visit(RootNode node) {
        node.accept(this);
    }
    
    void visit(DatabaseNode node);
    void visit(FilterNode node);
    void visit(JoinNode node);
    void visit(MinusNode node);
    void visit(OptionalNode node);
    void visit(PatternNode node);
    void visit(UnionNode node);

}
