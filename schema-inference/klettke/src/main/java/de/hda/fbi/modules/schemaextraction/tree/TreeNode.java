package de.hda.fbi.modules.schemaextraction.tree;

import de.hda.fbi.modules.schemaextraction.common.PropertyType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;

/**
 * Tree representation of a db collection containing all attributes ({@link TreeNode#children}) and every timestamp
 * ({@link TreeNode#treeNodeInfos}).
 * The representation consist of multiple {@link TreeNode}-Objects which are linked to build a tree structure.
 */
public class TreeNode implements Serializable {

    // Attributes of the entity
    private Vector<TreeNode> children = new Vector<TreeNode>();

    // Different attribute values of the timestamp-identifier which the collection contains.
    private Vector<TreeNodeInfo> treeNodeInfos = new Vector<TreeNodeInfo>();

    // Contains how the entries of the collection are stored (e.g. Json).
    private PropertyType propertyType;

    // Name of the collection.
    private String name;

    // Indicates the depth of the current node.
    private int level;

    // Parent node of the node.
    private TreeNode parent;

    public TreeNode(String name, int level, PropertyType propertyType, Object documentId, int timestamp,
                    TreeNode parent, Object value) {
        this.setName(name);
        this.setLevel(level);
        this.setPropertyType(propertyType);

        this.addTreeNodeInfo(documentId, timestamp, value);

        this.parent = parent;
    }

    public void addChild(TreeNode child) {
        this.children.addElement(child);
    }

    public void addChildren(TreeNode... aChildren) {
        this.children.addAll(Arrays.asList(aChildren));
    }

    public TreeNode getParent() {
        return this.parent;
    }

    /**
     * Returns the root node of the tree structure. The current object is returned if it is the root node of the tree.
     *
     * @return the root node of the tree.
     */
    public TreeNode getRoot() {
        if (this.parent == null) {
            return this;
        }
        TreeNode parent = this.parent;
        TreeNode root = null;

        while (parent != null) {
            root = parent;
            parent = parent.getParent();
        }

        return root;

    }

    /**
     * Returns all children nodes of the current node. If withDeep is true every child node is returned (recursive).
     *
     * @param withDeep indicates if ALL children nodes of the current node should be returned.
     * @return a collection with all found children.
     */
    public Vector<TreeNode> getChildren(boolean withDeep) {

        Vector<TreeNode> nodes = new Vector<TreeNode>();
        nodes.addAll(this.children);

        if (withDeep) {
            for (TreeNode child : this.children) {
                nodes.addAll(child.getChildren(true));
            }
        }

        return this.children;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public void addTreeNodeInfo(Object documentId, int timestamp, Object value) {
        this.treeNodeInfos.addElement(new TreeNodeInfo(documentId, timestamp, value));
    }

    /**
     * Checks if the provided parameters are equal to the attributes of the node.
     *
     * @param name         name of the node
     * @param propertyType type of the node
     * @param level        level of the node
     * @return the current node if the given parameters are equal to the attributes. Otherwise <b>null</b>
     */
    public TreeNode nodeEquals(String name, PropertyType propertyType, int level) {

        String currentName = this.getName();
        PropertyType currentPropertyType = this.getPropertyType();
        int currentLevel = this.getLevel();

        if (currentName.equals(name) && currentPropertyType.equals(propertyType) && currentLevel == level) {
            return this;
        } else {
            return null;
        }
    }

    /**
     * Returns the highest timestamp from all documents.
     *
     * @return the highest timestamp from all documents.
     */
    public Integer getMaximumTimestamp() {
        int maxTimestamp = 0;
        for (TreeNodeInfo info : this.getTreeNodeInfos()) {
            if (info.getTimestamp() > maxTimestamp) {
                maxTimestamp = info.getTimestamp();
            }
        }

        return maxTimestamp;
    }

    /**
     * Returns the next lower timestamp than the given one.
     *
     * @param timestamp
     * @return the next lower timestamp than the given one. If no lower timestamp is found <b>null</b> is returned.
     */
    public Integer getMaximumTimestampLowerThan(Integer timestamp) {
        Integer maxTimestamp = null;
        for (TreeNodeInfo info : this.getTreeNodeInfos()) {
            if (maxTimestamp == null) {
                if (info.getTimestamp() < timestamp) {
                    maxTimestamp = info.getTimestamp();

                }
            } else {
                if (info.getTimestamp() > maxTimestamp && info.getTimestamp() < timestamp) {
                    maxTimestamp = info.getTimestamp();

                }
            }
        }
        return maxTimestamp;
    }

    public Integer getNumberOfDocuments() {
        return this.treeNodeInfos.size();
    }

    public Vector<TreeNodeInfo> getTreeNodeInfos() {
        return treeNodeInfos;
    }

    public void setTreeNodeInfos(Vector<TreeNodeInfo> treeNodeInfos) {
        this.treeNodeInfos = treeNodeInfos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String toString() {
        return this.getName();
    }
}
