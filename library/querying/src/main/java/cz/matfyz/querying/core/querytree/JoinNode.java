package cz.matfyz.querying.core.querytree;

import cz.matfyz.querying.core.JoinCandidate;
import cz.matfyz.querying.core.JoinCandidate.SerializedJoinCandidate;

public class JoinNode extends QueryNode {

    public QueryNode fromChild() { return children.get(0); }
    public QueryNode setFromChild(QueryNode node) { return children.set(0, node); }
    public QueryNode toChild() { return children.get(1); }
    public QueryNode setToChild(QueryNode node) { return children.set(1, node); }
    public final JoinCandidate candidate;

    public JoinNode(QueryNode fromChild, QueryNode toChild, JoinCandidate candidate) {
        children.add(fromChild);
        children.add(toChild);
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
        SerializedJoinCandidate candidate
    ) implements SerializedQueryNode{

        @Override public String getType() { return "join"; }

    }

}
