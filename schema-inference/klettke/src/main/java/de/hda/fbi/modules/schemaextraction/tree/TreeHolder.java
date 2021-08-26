package de.hda.fbi.modules.schemaextraction.tree;

import java.util.Collections;
import java.util.Vector;

/**
 * Manages every tree representation of collections.
 */
public class TreeHolder {

    private static final TreeHolder treeHolder = new TreeHolder();

    private Vector<Tree> trees = new Vector<Tree>();

    public static TreeHolder getInstance() {
        return treeHolder;
    }

    // FIXME: ACHTUNG! Hier wird der Timestamp wie eine ID verwendet. Das bedeutet, dass immer nur ein EntityType mit einem eindeutigen Timestamp existieren kann! Um das zu ändern, müsste man auch auf den Namen oder eine ID prüfen.
    public Tree getTreeForTimestamp(int timestamp) {
        for (Tree tree : this.trees) {
            for (int tmpTimestamp : tree.getTimestamps()) {
                if (tmpTimestamp == timestamp) {
                    return tree;
                }
            }
        }

        return null;
    }

    public Vector<Tree> getAllTrees() {
        return this.trees;
    }

    public void createNewTree(String name) {
        TreeNode root = new TreeNode(name, 0, null, null, 0, null, "");

        Tree tree = new Tree(name, root);

        this.trees.addElement(tree);
    }

    public Vector<Integer> getAllUniqueTimestampsSorted() {
        Vector<Integer> allTimestamps = new Vector<Integer>();

        for (Tree tree : this.trees) {

            for (Integer ts : tree.getTimestamps()) {
                if (ts >= 0 && !allTimestamps.contains(ts)) {
                    allTimestamps.add(ts);
                }
            }
        }
        Collections.sort(allTimestamps);

        return allTimestamps;
    }

    public Tree getTree(String name) {
        for (Tree tree : this.trees) {
            if (tree.getName().equals(name)) {
                return tree;
            }
        }

        return null;
    }

    public void Clear() {
        this.trees.clear();
    }
}
