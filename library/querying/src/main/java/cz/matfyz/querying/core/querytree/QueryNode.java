package cz.matfyz.querying.core.querytree;

import cz.matfyz.core.querying.ResultStructure;
import cz.matfyz.core.utils.GraphUtils.TopDownTree;
import cz.matfyz.querying.optimizer.NodeCostData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class QueryNode implements TopDownTree<QueryNode> {

    protected ArrayList<QueryNode> children = new ArrayList<>();

    private @Nullable QueryNode parent = null;

    public @Nullable ResultStructure structure = null;

    public @Nullable NodeCostData predictedCostData = null;

    public @Nullable Double evaluationTimeInMs = null;

    public void setParent(@Nullable QueryNode parent) {
        this.parent = parent;
    }

    public abstract <T> T accept(QueryVisitor<T> visitor);

    public interface SerializedQueryNode extends Serializable {

        /** Will be automatically serialized as <code>type</code>. */
        String getType();

        @JsonProperty("structure")
        ResultStructure structure();

    }

    public Collection<QueryNode> children() { return children; }

    @Nullable public QueryNode parent() { return parent; }

    /**
     * Finds and replaces a given child (NOT indirect descendant) node.
     * Also sets the new child's parent accordingly, but leaves original child unmodified.
     */
    public boolean replaceChild(QueryNode originalChild, QueryNode replacementChild) {
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).equals(originalChild)) {
                children.set(i, replacementChild);
                replacementChild.setParent(this);
                return true;
            }
        }

        return false;
    }

}
