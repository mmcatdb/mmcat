package cz.matfyz.querying.core.querytree;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class QueryNode {
    
    @Nullable
    private QueryNode parent = null;

    void setParent(QueryNode parent) {
        this.parent = parent;
    }

    public abstract <T> T accept(QueryVisitor<T> visitor);

}
