package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.schema.SchemaMorphism.Min;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The {@code AccessTreeNode} class represents a node in a tree structure that
 * holds information about properties in a schema category. This is used to
 * construct an access path in the schema.
 */
public class AccessTreeNode {

    // FIXME The point of using a constant is to have one contant in the whole project. I.e., somewhere in the inference algorithms, there is bound to be used the "_" symbol. That one should be replaced with this constant.
    // Even better, the original algorithm should define the costant and this class should use it.
    private static final String ARRAY_SYMBOL = "_";

    /**
     * Enum representing the type of the {@code AccessTreeNode}.
     * <ul>
     *   <li>{@link #ROOT} - Represents the root node of the tree.</li>
     *   <li>{@link #SIMPLE} - Represents a simple node.</li>
     *   <li>{@link #COMPLEX} - Represents a complex node.</li>
     * </ul>
     */
    public enum Type {
        ROOT,
        SIMPLE,
        COMPLEX,
    }

    public final String name;
    @Nullable public final BaseSignature signature;
    public final Key key;
    @Nullable private Key parentKey;
    @Nullable public final String label;
    @Nullable public final Min min;
    public final boolean isArrayType;
    private List<AccessTreeNode> children;

    public AccessTreeNode(String name, @Nullable BaseSignature signature, Key key, @Nullable Key parentKey, @Nullable String label, @Nullable Min min, boolean isArrayType) {
        this.name = name;
        this.signature = signature;
        this.key = key;
        this.parentKey = parentKey;
        this.label = label;
        this.min = min;
        this.isArrayType = isArrayType;
        this.children = new ArrayList<>();
    }

    public Type getType() {
        if (parentKey == null)
            return Type.ROOT;
        if (!children.isEmpty())
            return Type.COMPLEX;
        return Type.SIMPLE;
    }

    public Key getParentKey() {
        return parentKey;
    }

    public List<AccessTreeNode> getChildren() {
        return children;
    }

    /**
     * Adds a child node to the list of children.
     */
    public void addChild(AccessTreeNode child) {
        children.add(child);
    }

    /**
     * Finds a node with the specified key starting from the given node.
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
     * If the node is of type {@code isArrayType}, it checks its children for any node named ARRAY_SYMBOL.
     * It removes the intermediate ARRAY_SYMBOL node and promotes its children to be direct children
     * of the {@code isArrayType} node.
     */
    public void transformArrayNodes() {
        if (this.isArrayType && !this.children.isEmpty()) {
            final AccessTreeNode child = this.children.get(0); // getting the first child
            if (child.name.equals(ARRAY_SYMBOL))
                promoteChildren(child);
        }

        for (final AccessTreeNode child : this.children)
            child.transformArrayNodes();
    }

    private void promoteChildren(AccessTreeNode child) {
        final List<AccessTreeNode> newChildren = new ArrayList<>(child.getChildren());
        for (final AccessTreeNode newChild : newChildren)
            newChild.parentKey = this.key;

        this.children = newChildren;
    }

    public void printTree(String prefix) {
        System.out.println(prefix + "Name: " + this.name +
                                    ", Type: " + this.getType() +
                                    ", Signature: " + (this.signature != null ? this.signature.toString() : "None") +
                                    ", Key: " + this.key +
                                    ", Parent Key: " + this.parentKey +
                                    ", isArrayType: " + this.isArrayType);

        for (AccessTreeNode child : this.children)
            child.printTree(prefix + "    ");
    }
}
