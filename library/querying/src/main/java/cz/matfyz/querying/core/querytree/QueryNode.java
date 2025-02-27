package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.utils.GraphUtils.TopDownTree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class QueryNode implements TopDownTree<QueryNode> {

    protected ArrayList<QueryNode> children = new ArrayList<>();

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

    public Collection<QueryNode> children() { return children; }

    @Nullable public QueryNode parent() { return parent; }

    /**
     * Finds and replaces a given child (NOT indirect descendant) node.
     * Also sets parents accordingly, including removing original child's parent.
     */
    public boolean replaceChild(QueryNode originalChild, QueryNode replacementChild) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).equals(originalChild)) {
                children.set(i, replacementChild);
                replacementChild.setParent(this);
                originalChild.setParent(null);
                return true;
            }
        }

        return false;
    }

}
