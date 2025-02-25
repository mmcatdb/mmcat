package cz.matfyz.querying.core.querytree;

public class MinusNode extends QueryNode {

    public QueryNode primaryChild() { return children().get(0); }
    public QueryNode setPrimaryChild(QueryNode node) { return children().set(0, node); }
    public QueryNode minusChild() { return children().get(1); }
    public QueryNode setMinusChild(QueryNode node) { return children().set(1, node); }

    public MinusNode(QueryNode primaryChild, QueryNode minusChild) {
        children().add(primaryChild);
        children().add(minusChild);

        primaryChild.setParent(this);
        minusChild.setParent(this);
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedMinusNode(
        SerializedQueryNode primaryChild,
        SerializedQueryNode minusChild
    ) implements SerializedQueryNode{

        @Override public String getType() { return "minus"; }

    }

}
