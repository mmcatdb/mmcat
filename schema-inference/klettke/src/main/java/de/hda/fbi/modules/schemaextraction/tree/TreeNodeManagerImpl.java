package de.hda.fbi.modules.schemaextraction.tree;

import de.hda.fbi.modules.schemaextraction.common.PropertyType;
import org.springframework.stereotype.Component;

@Component
public class TreeNodeManagerImpl implements TreeNodeManager {

    public void storeIfNotExists(TreeNode root, String name, String parentName, PropertyType propertyType, int level,
                                 Object documentId, int timestamp, Object value) {

        if (level <= 0) {
            root.getRoot().setName(name);
            root.getRoot().setPropertyType(propertyType);
            root.getRoot().addTreeNodeInfo(documentId, timestamp, value);
            return;
        }

        if (root.getLevel() + 1 == level && root.getName().equals(parentName)) {

            TreeNode existingNode = null;

            for (TreeNode node : root.getChildren(true)) {

                if (existingNode == null) {
                    existingNode = node.nodeEquals(name, propertyType, level);
                }
            }

            // Der Knoten existiert bereits
            // Daher die Informationen hinzufügen
            if (existingNode != null) {
                existingNode.addTreeNodeInfo(documentId, timestamp, value);
                return;
            } else {
                // Knoten existiert noch nicht. Also neu anlegen.
                root.addChild(new TreeNode(name, level, propertyType, documentId, timestamp, root, value));
                return;
            }
        }

        for (TreeNode node : root.getChildren(true)) {
            this.storeIfNotExists(node, name, parentName, propertyType, level, documentId, timestamp, value);
        }
    }

}
