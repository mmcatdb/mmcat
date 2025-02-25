package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.utils.GraphUtils.TopDownTree;

import java.io.Serializable;
import java.util.ArrayList;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class QueryNode implements TopDownTree<QueryNode> {

    private ArrayList<QueryNode> children = new ArrayList<>();

    @Nullable
    private QueryNode parent = null;

    @Nullable
    public ResultStructure structure = null;

    public void setParent(@Nullable QueryNode parent) {
        this.parent = parent;
    }

    public abstract <T> T accept(QueryVisitor<T> visitor);

    public interface SerializedQueryNode extends Serializable {

        /** Will be automatically serialized as `type`. */
        String getType();

    }

    public ArrayList<QueryNode> children() { return children; }

    @Nullable public QueryNode parent() { return parent; }

}
