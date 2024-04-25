package cz.matfyz.querying.core.querytree;

import cz.matfyz.abstractwrappers.datasource.Datasource;

/**
 * The whole subtree starting with the child node can be processed withing the specified datasource.
 * There has to be exactly one datasource node on each path from the global root node to any leaf.
 */
public class DatasourceNode extends QueryNode {

    public final QueryNode child;
    public final Datasource datasource;

    public DatasourceNode(QueryNode child, Datasource datasource) {
        this.child = child;
        this.datasource = datasource;

        child.setParent(this);
    }

    @Override public <T> T accept(QueryVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
