package de.hda.fbi.modules.schemaextraction.tree;

import de.hda.fbi.modules.schemaextraction.common.PropertyType;

public interface TreeNodeManager {

    void storeIfNotExists(TreeNode root, String name, String parentName, PropertyType propertyType, int level,
                          Object documentId, int timestamp, Object value);
}
