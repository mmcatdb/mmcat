package de.hda.fbi.modules.schemaextraction.tree;

import de.hda.fbi.modules.schemaextraction.common.PropertyType;

import java.util.Vector;

public interface TreeNodeInfoService {

    Vector<TreeNode> getAddedNodes(TreeNode root, Integer lastTimestamp, Integer currentTimestamp);

    Vector<TreeNode> getDeletedNodes(TreeNode root, Integer lastTimestamp, Integer currentTimestamp);

    Vector<TreeNode> getNodesWithTimestamp(TreeNode root, Integer timestamp);

    Vector<TreeNode> getNodesBeforeTimestamp(TreeNode root, Integer timestamp);

    TreeNode getLastTreeNodeBefore(TreeNode root, Integer timestamp, String name, PropertyType propertyType, int level);

    TreeNode getFirstTreeNodeAfter(TreeNode root, Integer timestamp, String name, PropertyType propertyType, int level);
}
