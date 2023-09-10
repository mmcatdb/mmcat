package cz.matfyz.querying.core.querytree;

import cz.matfyz.abstractwrappers.database.Database;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class QueryNode {
    
    @Nullable
    private QueryNode parent = null;

    void setParent(QueryNode parent) {
        this.parent = parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    /**
     * This field tells us that the whole subtree rooting in this node can be processed withing the specified database system.
     * There has to be exactly one node with database on each path from the global root node to any leaf.
     */
    @Nullable
    private Database database;

    public void setDatabase(Database database) {
        this.database = database;
    }

}
