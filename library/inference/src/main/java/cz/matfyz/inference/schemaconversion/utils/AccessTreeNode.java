package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.schema.SchemaMorphism.Min;

// FIXME The javadoc doesn't render new lines, so there's no point in breaking the lines like this. They should be breaked to separate logical points, not just to fit the line length.
/**
 * The {@code AccessTreeNode} class represents a node in a tree structure that
 * holds information about properties in a schema category. This is used to
 * construct an access path in the schema.
 */
public class AccessTreeNode {

    // FIXME Why is it named "state"?. It looks more like "type of the node". State is something that changes over time.
    // Also, isn't the simple vs. complex type determined by the presence of children? And the root type by the absence of a parent?
    // If so, it would be better to just have a "getState" method that would compute the state based on the presence of children and parent.
    /**
     * Enum representing the state of the {@code AccessTreeNode}.
     * <ul>
     *   <li>{@link #ROOT} - Represents the root node of the tree.</li>
     *   <li>{@link #SIMPLE} - Represents a simple node.</li>
     *   <li>{@link #COMPLEX} - Represents a complex node.</li>
     * </ul>
     */
    public enum State {
        ROOT,
        SIMPLE,
        COMPLEX,
    }

    private State state;
    private final String name;
    private final BaseSignature signature;
    private final Key key;
    // FIXME This looks like it might be null. If so, it should be annotated with @Nullable from org.checkerframework.checker.nullness.qual.Nullable. This applies to all fields or function arguments that can be null.
    private Key parentKey;
    private final String label;
    private final Min min;
    private final boolean isArrayType;
    private List<AccessTreeNode> children;

    /**
     * Constructs a new {@code AccessTreeNode} with the specified parameters.
     *
     * @param state The state of the node.
     * @param name The name of the node.
     * @param signature The base signature associated with the node.
     * @param key The key of the node.
     * @param parentKey The key of the parent node.
     * @param label The label of the node.
     * @param min The minimum cardinality of the node.
     * @param isArrayType A flag indicating if the node represents an array type.
     */
    public AccessTreeNode(State state, String name, BaseSignature signature, Key key, Key parentKey, String label, Min min, boolean isArrayType) {
        this.state = state;
        this.name = name;
        this.signature = signature;
        this.key = key;
        this.parentKey = parentKey;
        this.label = label;
        this.min = min;
        this.isArrayType = isArrayType;
        this.children = new ArrayList<>();
    }

    public State getState() {
        return state;
    }

    // FIXME Similarly to the comments below, it's simpler to just make the value public.
    public void setState(State newState) {
        this.state = newState;
    }

    // FIXME It doesn't make sense to have a getter for something that is final. It's better to make it public and remove the getter.
    public String getName() {
        return name;
    }

    public BaseSignature getSignature() {
        return signature;
    }

    public Key getKey() {
        return key;
    }

    public Key getParentKey() {
        return parentKey;
    }

    // FIXME Is this method necessary? If it's only intended to be used internally, it shouldn't be there.
    public void setParentKey(Key parentKey) {
        this.parentKey = parentKey;
    }

    public String getLabel() {
        return label;
    }

    public Min getMin() {
        return min;
    }

    public boolean getIsArrayType() {
        return isArrayType;
    }

    public List<AccessTreeNode> getChildren() {
        return children;
    }

    /**
     * Adds a child node to the list of children.
     *
     * @param child The child node to add.
     */
    public void addChild(AccessTreeNode child) {
        children.add(child);
    }

    /**
     * Finds a node with the specified key starting from the given node.
     *
     * @param targetKey The key to search for.
     * @param node The starting node for the search.
     * @return The {@code AccessTreeNode} with the specified key.
     * @throws NoSuchElementException If a node with the specified key is not found.
     */
    public static AccessTreeNode findNodeWithKey(Key targetKey, AccessTreeNode node) throws NoSuchElementException {
        if (node.key.equals(targetKey))
            return node;

        for (final AccessTreeNode child : node.children) {
            AccessTreeNode result = findNodeWithKey(targetKey, child);
            if (result != null)
                return result;
        }

        throw new NoSuchElementException("Node with key " + targetKey + " not found.");
    }

    /**
     * Transforms the tree structure starting from this node by handling array nodes.
     * If the node is of type {@code isArrayType}, it checks its children for any node named "_".
     * It removes the intermediate "_" node and promotes its children to be direct children
     * of the {@code isArrayType} node.
     */
    public void transformArrayNodes() {
        if (this.isArrayType && !this.children.isEmpty()) {
            final AccessTreeNode child = this.children.get(0); // getting the first child
            // FIXME This "_" character looks like some magical constant that is used on multiple places. If so, it should be extracted to a constant.
            if (child.getName().equals("_"))
                promoteChildren(child);
        }

        for (final AccessTreeNode child : this.children)
            child.transformArrayNodes();
    }

    /**
     * Promotes the children of a specified child node to be a direct child of the current node.
     *
     * @param child The child node whose children are to be promoted.
     */
    private void promoteChildren(AccessTreeNode child) {
        final List<AccessTreeNode> newChildren = new ArrayList<>(child.getChildren());
        for (final AccessTreeNode newChild : newChildren)
            newChild.setParentKey(this.key);

        this.children = newChildren;
    }

    public void printTree(String prefix) {
        System.out.println(prefix + "Name: " + this.name +
                                    ", State: " + this.state +
                                    ", Signature: " + (this.signature != null ? this.signature.toString() : "None") +
                                    ", Key: " + this.key +
                                    ", Parent Key: " + this.parentKey +
                                    ", isArrayType: " + this.isArrayType);

        for (AccessTreeNode child : this.children)
            child.printTree(prefix + "    ");
    }
}
