package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.querying.ResultStructure;

import java.util.List;

public class UnionNode extends QueryNode {

    public UnionNode(QueryNode... children) {

        for (final var c : children) {
            c.setParent(this);
            this.children.add(c);
        };
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public record SerializedUnionNode(
        ResultStructure structure,
        List<SerializedQueryNode> children
    ) implements SerializedQueryNode {

        @Override public String getType() { return "union"; }

    }

}
