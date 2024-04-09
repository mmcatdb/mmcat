package cz.matfyz.inference.schemaconversion.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaMorphism.Min;

/**
 * Class to hold info about properties in SchemaCat, so that an access path can be
 * later constructed.
 *
 */
public class AccessTreeNode{

    public enum State {Root,Simple, Complex;} 

    public State state;
    public String name;
    public Integer sigVal;
    public List<AccessTreeNode> children;
    public Key key;
    public Key parentKey;
    public String label;
    public Min min;
    public Signature sig;

    public AccessTreeNode(State state, String name, Integer sigVal, Key key, Key parentKey, String label, Min min) {
        this.state = state;
        this.name = name;
        this.sigVal = sigVal; // a node contains signature between itself and its parent
        this.children = new ArrayList<>();
        this.key = key;
        this.parentKey = parentKey;
        this.label = label;
        this.min = min;
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
    public int getSigVal() {
        return sigVal;
    }
    public Key getKey() {
        return key;
    }
    public Key getParentKey() {
        return parentKey;
    }
    public String getLabel() {
        return label;
    }
    public Min getMin() {
        return min;
    }
    public Signature getSig() {
        return sig;
    }

    public AccessTreeNode findNodeWithName(String targetName) {
        if (this.name.equals(targetName)) {
            return this;
        }
        for (AccessTreeNode child : this.children) {
            if (child.findNodeWithName(targetName) != null) {
                return child;
            }
        }
        return null;
    }

    public void printTree(String prefix) {
        System.out.println(prefix + "Name: " + this.name + ", State: " + this.state + ", Signature: " + (this.sig != null ? this.sig.toString() : "None"));
        for (AccessTreeNode child : this.children) {
            child.printTree(prefix + "    ");
        }
    }
    
    public static void assignSignatures(AccessTreeNode node, Map<Integer, Integer> signatureOrder) {
        if (node.state == State.Root) {
            return;
        }

        Integer newSigVal = signatureOrder.get(node.getSigVal());
        node.sig = Signature.createBase(newSigVal);

        for (AccessTreeNode child : node.getChildren()) {
            assignSignatures(child, signatureOrder);
        }
    }


}

