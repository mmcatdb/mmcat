package cz.matfyz.querying.core.querytree;

public class OptionalNode extends QueryNode {

    public QueryNode primaryChild() { return children().get(0); }
    public QueryNode setPrimaryChild(QueryNode node) { return children().set(0, node); }
    public QueryNode optionalChild() { return children().get(1); }
    public QueryNode setOptionalChild(QueryNode node) { return children().set(1, node); }

    public OptionalNode(QueryNode primaryChild, QueryNode optionalChild) {
        children().add(primaryChild);
        children().add(optionalChild);

        primaryChild.setParent(this);
        optionalChild.setParent(this);
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedOptionalNode(
        SerializedQueryNode primaryChild,
        SerializedQueryNode optionalChild
    ) implements SerializedQueryNode{

        @Override public String getType() { return "optional"; }

    }

}
