package cz.matfyz.querying.core.querytree;

import cz.matfyz.querying.core.JoinCandidate;

public class JoinNode extends QueryNode {

    public final QueryNode fromChild;
    public final QueryNode toChild;
    public final JoinCandidate candidate;

    public JoinNode(QueryNode fromChild, QueryNode toChild, JoinCandidate candidate) {
        this.fromChild = fromChild;
        this.toChild = toChild;
        this.candidate = candidate;

        fromChild.setParent(this);
        toChild.setParent(this);
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedJoinNode(
        SerializedQueryNode fromChild,
        SerializedQueryNode toChild,
        String candidate
    ) implements SerializedQueryNode{

        @Override public String getType() { return "join"; }

    }

}
