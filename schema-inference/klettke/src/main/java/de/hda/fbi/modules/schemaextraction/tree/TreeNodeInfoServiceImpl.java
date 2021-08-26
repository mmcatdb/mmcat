package de.hda.fbi.modules.schemaextraction.tree;

import de.hda.fbi.modules.schemaextraction.common.PropertyType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

@Component
public class TreeNodeInfoServiceImpl implements TreeNodeInfoService {

    /**
     * Returns nodes (recursively)  that exist in a previous version (lastTimestamp) but were removed in a later
     * version (currentTimestamp). If no deleted nodes were found an empty list is returned.
     * <p>
     * The get all deleted nodes from a tree use tree.getRoot() as currentNode, 0 as lastTimestamp and the lowest
     * timestamp of the tree as currentTimestamp.
     *
     * @param currentNode      the node from were the children nodes are extracted.
     * @param lastTimestamp    the previous timestamp that is compared to the current timestamp.
     * @param currentTimestamp the current timestamp which is compared against the previous timestamp.
     * @return a list containing all deleted nodes (attributes of the collection). If no deleted nodes are found
     * an empty list is returned.
     */
    public Vector<TreeNode> getDeletedNodes(TreeNode currentNode, Integer lastTimestamp, Integer currentTimestamp) {
        if (currentTimestamp == 0) {
            // recursive break criteria
            return new Vector<TreeNode>();
        }
        Vector<TreeNode> deletedNodes = new Vector<TreeNode>();
        //iterate over all attributes and check if one or multiple attributes are removed based on timestamp differentiation
        for (TreeNode node : currentNode.getChildren(true)) {
            if (checkIfNodeWasDeleted(node, lastTimestamp, currentTimestamp)) {
                deletedNodes.add(node);
            }
            // recursively iterate over all children nodes of the current children (recursively)
            deletedNodes.addAll(this.getDeletedNodes(node, lastTimestamp, currentTimestamp));
        }
        return deletedNodes;
    }

    /**
     * Returns nodes (recursively)  that didn't exist in a previous version (lastTimestamp) but were added in a later
     * version (currentTimestamp). If no added nodes were found an empty list is returned.
     * <p>
     * The get all added nodes from a tree use tree.getRoot() as currentNode, 0 as lastTimestamp and the lowest
     * timestamp of the tree as currentTimestamp.
     *
     * @param currentNode      the node from were the children nodes are extracted.
     * @param lastTimestamp    the previous timestamp that is compared to the current timestamp.
     * @param currentTimestamp the current timestamp which is compared against the previous timestamp.
     * @return a list containing all added nodes (attributes of the collection). If no added nodes are found
     * an empty list is returned.
     */
    public Vector<TreeNode> getAddedNodes(TreeNode currentNode, Integer lastTimestamp, Integer currentTimestamp) {
        Vector<TreeNode> addedNodes = new Vector<TreeNode>();
        if (currentNode.getParent() == null && lastTimestamp == 0) {
            for (TreeNodeInfo treeNodeInfo : currentNode.getTreeNodeInfos()) {
                if (treeNodeInfo.getTimestamp() == currentTimestamp && !addedNodes.contains(currentNode)) {
                    addedNodes.add(currentNode);
                }
            }
        }
        for (TreeNode node : currentNode.getChildren(true)) {
            if (checkIfNodeWasAdded(node, lastTimestamp, currentTimestamp)) {
                addedNodes.add(node);
            }
            addedNodes.addAll(this.getAddedNodes(node, lastTimestamp, currentTimestamp));
        }
        return addedNodes;
    }

    /**
     * Checks if the node was deleted in the given time step.
     *
     * @param node             the node (attribute) that should be checked.
     * @param lastTimestamp    the previous version which is compared against the current version.
     * @param currentTimestamp the current version which is compared against the previous version.
     * @return true if the node was deleted in this time step. False if not.
     */
    private boolean checkIfNodeWasDeleted(TreeNode node, Integer lastTimestamp, Integer currentTimestamp) {
        Boolean addedOrDeleted = checkIfNodeIsDeletedOrAdded(node, lastTimestamp, currentTimestamp);
        if (addedOrDeleted != null) {
            return !addedOrDeleted;
        }
        return false;
    }

    /**
     * Checks if the node was added in the given time step.
     *
     * @param node             the node (attribute) that should be checked.
     * @param lastTimestamp    the previous version which is compared against the current version.
     * @param currentTimestamp the current version which is compared against the previous version.
     * @return true if the node was added in this time step. False if not.
     */
    private boolean checkIfNodeWasAdded(TreeNode node, Integer lastTimestamp, Integer currentTimestamp) {
        Boolean addedOrDeleted = checkIfNodeIsDeletedOrAdded(node, lastTimestamp, currentTimestamp);
        if (addedOrDeleted != null) {
            return addedOrDeleted;
        }
        return false;
    }


    /**
     * Returns a boolean object that indicates if the given node was added or deleted at the given time step.
     * Returns null if it was neither was added or deleted at some point.
     *
     * @param node             the node (attribute) that should be checked.
     * @param lastTimestamp    the previous version which is compared against the current version.
     * @param currentTimestamp the current version which is compared against the previous version.
     * @return true if the node was added. False if the node was deleted. Null if the node was neither deleted or added.
     */
    private Boolean checkIfNodeIsDeletedOrAdded(TreeNode node, Integer lastTimestamp, Integer currentTimestamp) {
        boolean lastTimestampExists = false;
        boolean currentTimestampExists = false;

        lastTimestampExists = node.getTreeNodeInfos().stream()
                .map(TreeNodeInfo::getTimestamp)
                .anyMatch(timestamp -> timestamp.equals(lastTimestamp));
        currentTimestampExists = node.getTreeNodeInfos().stream()
                .map(TreeNodeInfo::getTimestamp)
                .anyMatch(timestamp -> timestamp.equals(currentTimestamp));

        if (!lastTimestampExists && currentTimestampExists) {
            //node was added
            return true;
        }
        if (lastTimestampExists && !currentTimestampExists) {
            //node was deleted
            return false;
        }
        //neither of both
        return null;
    }

    /**
     * Returns a list of nodes that exist before a given timestamp.
     *
     * @param root      the root node of the tree.
     * @param timestamp the timestamp which nodes are checked against.
     * @return a list containing all nodes that exist before the given timestamp.
     * If no node was found an empty list is returned.
     */
    public Vector<TreeNode> getNodesBeforeTimestamp(TreeNode root, Integer timestamp) {
        Vector<TreeNode> nodes = new Vector<TreeNode>();
        for (TreeNode node : root.getChildren(true)) {
            for (TreeNodeInfo treeNodeInfo : node.getTreeNodeInfos()) {
                if (treeNodeInfo.getTimestamp() < timestamp) {
                    nodes.addElement(node);
                    break;
                }
            }
        }
        return nodes;
    }

    /**
     * Returns a list with nodes with the given timestamp.
     *
     * @param root      the root node of the tree.
     * @param timestamp the timestamp which nodes are checked against.
     * @return a list of nodes with the given timestamp. If no nodes were found an empty list is returned.
     */
    public Vector<TreeNode> getNodesWithTimestamp(TreeNode root, Integer timestamp) {
        Vector<TreeNode> nodes = new Vector<TreeNode>();
        for (TreeNode node : root.getChildren(true)) {
            for (TreeNodeInfo treeNodeInfo : node.getTreeNodeInfos()) {
                if (treeNodeInfo.getTimestamp() == timestamp) {
                    nodes.addElement(node);
                    break;
                }
            }
        }
        return nodes;
    }

    /**
     * @param root
     * @param timestamp
     * @param name
     * @param propertyType
     * @param level
     * @return
     */
    public TreeNode getLastTreeNodeBefore(TreeNode root, Integer timestamp, String name, PropertyType propertyType,
                                          int level) {

        Vector<TreeNode> matchedTreeNodes = new Vector<TreeNode>();

        // Alle Knoten holen die den Kriterien entsprechen
        for (TreeNode node : root.getChildren(true)) {
            if (node.nodeEquals(name, propertyType, level) != null) {
                matchedTreeNodes.addElement(node);
            }
        }

        // Nun nur den Knoten holen, dessen Timestamp kleiner als die angegebene
        // ist und dennoch von allen die hoechste Timestamp hat
        Collections.sort(matchedTreeNodes, new Comparator<TreeNode>() {
            public int compare(TreeNode tr1, TreeNode tr2) {
                return tr1.getMaximumTimestamp().compareTo(tr2.getMaximumTimestamp());
            }
        });

        if (matchedTreeNodes.size() > 0) {
            TreeNode relevantNode = matchedTreeNodes.get(matchedTreeNodes.size() - 1);
            if (relevantNode.getMaximumTimestamp() < timestamp) {
                return relevantNode;
            } else {
                return null;
            }

        } else {
            return null;
        }

    }

    /**
     * @param root
     * @param timestamp
     * @param name
     * @param propertyType
     * @param level
     * @return
     */
    public TreeNode getFirstTreeNodeAfter(TreeNode root, Integer timestamp, String name, PropertyType propertyType,
                                          int level) {

        Vector<TreeNode> matchedTreeNodes = new Vector<TreeNode>();

        // Alle Knoten holen die den Kriterien entsprechen
        for (TreeNode node : root.getChildren(true)) {
            if (node.nodeEquals(name, propertyType, level) != null) {
                matchedTreeNodes.addElement(node);
            }
        }

        // Nun nur den Knoten holen, dessen Timestamp gr��er als die
        // angegebene ist und dennoch von allen die kleinste Timestamp hat
        Collections.sort(matchedTreeNodes, new Comparator<TreeNode>() {
            public int compare(TreeNode tr1, TreeNode tr2) {
                return tr2.getMaximumTimestamp().compareTo(tr1.getMaximumTimestamp());
            }
        });

        if (matchedTreeNodes.size() > 0) {
            TreeNode relevantNode = matchedTreeNodes.get(matchedTreeNodes.size() - 1);
            if (relevantNode.getMaximumTimestamp() > timestamp) {
                return relevantNode;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }
}
