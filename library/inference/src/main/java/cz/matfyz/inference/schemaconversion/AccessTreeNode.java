package cz.matfyz.inference.schemaconversion;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.identifiers.Signature;

/**
 * Class to hold info about properties in SchemaCat, so that an access path can be
 * later constructed.
 *
 */
public class AccessTreeNode{

    public enum State {S, C;} // S - simple property, C - complex property

    public State state;
    public String name;
    public Signature sig;
    public List<AccessTreeNode> children;

    public AccessTreeNode(State state, String name, Signature sig) {
        this.state = state;
        this.name = name;
        this.sig = sig; // a node contains signature between itself and its parent
        this.children = new ArrayList<>();
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


}

