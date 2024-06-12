package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.schema.SchemaMorphism.Min;

/**
 * Class to hold info about properties in SchemaCategory, so that an access path can be constructed.
 */
public class AccessTreeNode {

    public enum State {
        ROOT,
        SIMPLE,
        COMPLEX,
    }

    private State state;
    private final String name;
    private final BaseSignature signature;
    private final Key key;
    private Key parentKey;
    private final String label;
    private final Min min;
    private final boolean isArrayType;
    private List<AccessTreeNode> children;

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
    public void setState(State newState) {
        this.state = newState;
    }

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
    public void addChild(AccessTreeNode child) {
        children.add(child);
    }

    public static AccessTreeNode findNodeWithKey(Key targetKey, AccessTreeNode node) throws Exception {
        if (node.key.equals(targetKey)) {
            return node;
        }
        for (AccessTreeNode child : node.children) {
            AccessTreeNode result = findNodeWithKey(targetKey, child);
            if (result != null) {
                return result;
            }
        }
        throw new NoSuchElementException("Node with key " + targetKey + " not found.");
    }

    /**
     * This method traverses the tree starting from the current node.
        If the node is of type isArrayType, it checks its children for any node named "_".
        It removes the intermediate "_" node and promotes its children to be direct children of the isArrayType node.
        It then recursively applies this transformation to all children of the current node.
     */
    public void transformArrayNodes() {
        if (this.isArrayType) {
            AccessTreeNode child = this.children.get(0); // getting the first child
            if (child.getName().equals("_")) {
                promoteChildren(child);
            }
        }
        for (AccessTreeNode child : this.children) {
            child.transformArrayNodes();
        }
    }

    private void promoteChildren(AccessTreeNode child) {
        List<AccessTreeNode> newChildren = new ArrayList<>(child.getChildren());
        for (AccessTreeNode newChild : newChildren) {
            newChild.setParentKey(this.key);
        }
        this.children = newChildren;
    }

    public void printTree(String prefix) {
        System.out.println(prefix + "Name: " + this.name +
                                    ", State: " + this.state +
                                    ", Signature: " + (this.signature != null ? this.signature.toString() : "None") +
                                    ", Key: " + this.key +
                                    ", Parent Key: " + this.parentKey +
                                    ", isArrayType: " + this.isArrayType);
        for (AccessTreeNode child : this.children) {
            child.printTree(prefix + "    ");
        }
    }
}

