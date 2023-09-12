package cz.matfyz.querying.core.querytree;

import cz.matfyz.abstractwrappers.database.Database;

/**
 * The whole subtree starting with the child node can be processed withing the specified database system.
 * There has to be exactly one database node on each path from the global root node to any leaf.
 */
public class DatabaseNode extends QueryNode {

    public final QueryNode child;
    public final Database database;

    public DatabaseNode(QueryNode child, Database database) {
        this.child = child;
        this.database = database;

        child.setParent(this);
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }

}
