package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.schema.SchemaMorphism.Min;

/**
 * Class to hold info about properties in SchemaCat, so that an access path can be
 * later constructed.
 */
public class AccessTreeNode {

    public enum State {
        Root,
        Simple,
        Complex,
    }

    public State state;
    public String name;
    public List<AccessTreeNode> children;
    public Key key;
    public Key parentKey;
    public String label;
    public Min min;
    public BaseSignature sig;
    public boolean isArrayType;

    public AccessTreeNode(State state, String name, BaseSignature signature, Key key, Key parentKey, String label, Min min, boolean isArrayType) {
        this.state = state;
        this.name = name;
        this.sig = signature;
        this.children = new ArrayList<>();
        this.key = key;
        this.parentKey = parentKey;
        this.label = label;
        this.min = min;
        this.isArrayType = isArrayType;
    }

    public void addChild(AccessTreeNode child) {
        children.add(child);
    }
    public List<AccessTreeNode> getChildren() {
        return children;
    }
    public State getState() {
        return state;
    }
    public String getName() {
        return name;
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
    public BaseSignature getSig() {
        return sig;
    }
    public boolean getIsArrayType() {
        return isArrayType;
    }

    public static AccessTreeNode findNodeWithKey(Key targetKey, AccessTreeNode node) {
        if (node.key.equals(targetKey)) {
            return node;
        }
        for (AccessTreeNode child : node.children) {
            AccessTreeNode result = findNodeWithKey(targetKey, child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * This method traverses the tree starting from the current node.
        If the node is of type isArrayType, it checks its children for any node named "_".
        It removes the intermediate "_" node and promotes its children to be direct children of the isArrayType node.
        It then recursively applies this transformation to all children of the current node.
     */
    public void transformArrayNodes() {
        if (this.isArrayType) {
            // get the first child
            AccessTreeNode child = this.children.get(0);
            if (child.getName().equals("_")) {
                List<AccessTreeNode> newChildren = new ArrayList<>(child.getChildren());
                for (AccessTreeNode newChild : newChildren) {
                    newChild.setParentKey(this.key);
                }
                this.children = newChildren;
            }
        }
        for (AccessTreeNode child : this.children) {
            child.transformArrayNodes();
        }
    }

    /**
     * Check if all keys in the tree are unique
     * (Maybe merge with the other method so that you dont traverse the tree twice?)
     */
    public boolean areKeysUnique() {
        Set<Key> keySet = new HashSet<>();
        return areKeysUniqueHelper(this, keySet);
    }

    private boolean areKeysUniqueHelper(AccessTreeNode node, Set<Key> keySet) {
        if (node.key != null) {
            if (keySet.contains(node.key)) {
                return false;
            }
            keySet.add(node.key);
        }
        for (AccessTreeNode child : node.children) {
            if (!areKeysUniqueHelper(child, keySet)) {
                return false;
            }
        }
        return true;
    }

    public void printTree(String prefix) {
        System.out.println(prefix + "Name: " + this.name + ", State: " + this.state + ", Signature: " + (this.sig != null ? this.sig.toString() : "None") + ", Key: " + this.key + ", Parent Key: " + this.parentKey + ", isArrayType: " + this.isArrayType);
        for (AccessTreeNode child : this.children) {
            child.printTree(prefix + "    ");
        }
    }

}

