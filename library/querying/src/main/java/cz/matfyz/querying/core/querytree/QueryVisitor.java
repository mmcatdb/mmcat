package cz.matfyz.querying.core.querytree;

public interface QueryVisitor<T> {

    T visit(DatabaseNode node);
    T visit(FilterNode node);
    T visit(JoinNode node);
    T visit(MinusNode node);
    T visit(OptionalNode node);
    T visit(PatternNode node);
    T visit(UnionNode node);

}
