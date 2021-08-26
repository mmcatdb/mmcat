package de.hda.fbi.modules.schemaextraction.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.hda.fbi.modules.schemaextraction.commandline.ProgressBar;
import de.hda.fbi.modules.schemaextraction.common.EntityInfo;
import de.hda.fbi.modules.schemaextraction.common.PropertyType;
import de.hda.fbi.modules.schemaextraction.common.PropertyTypeTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class TreeBuilder {

//    private static Logger LOGGER = LoggerFactory.getLogger(TreeBuilder.class);

    public static final List<String> blacklist = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("_id");
                add("$oid");
            }});

    private JsonParser parser = new JsonParser();
    private PropertyTypeTranslator propertyTypeTranslator;
    private TreeNodeManager treeNodeManager;

    public TreeBuilder(TreeNodeManager treeNodeManager) {
        this.treeNodeManager = treeNodeManager;
        this.propertyTypeTranslator = new PropertyTypeTranslator();
    }

    public void build(List<EntityInfo> documentInfos) {
//        LOGGER.debug("Start building Tree!");
        ProgressBar progressBar = new ProgressBar();
        int i = 0;
        for (EntityInfo docInfo : documentInfos) {
            progressBar.update(++i, documentInfos.size());
            this.buildAndStore(docInfo.getEntityName(), docInfo.getEntityName(),
                    parser.parse(docInfo.getEntity().toString()), docInfo.getEntityName(), 0, docInfo.getId(),
                    docInfo.getTimestamp());

            // nur wenn der DocumentInfo nicht aus einem Schema (inkrementell
            // erste Timestamp) entstanden ist
            // if (docInfo.isInitialDocument()) {
            Tree tree = TreeHolder.getInstance().getTree(docInfo.getEntityName());
            tree.addTimestamp(docInfo.getTimestamp());
            tree.increaseNumberOfDocuments();

            // }
        }
//        LOGGER.debug("Building Tree done!");
    }

    private void buildAndStore(String entityType, String parentName, JsonElement node, String nodeName, int level,
                               Object documentId, int timestamp) {

        Tree tree = TreeHolder.getInstance().getTree(entityType);

        if (tree == null) {
            TreeHolder.getInstance().createNewTree(entityType);
            tree = TreeHolder.getInstance().getTree(entityType);
        }

        PropertyType propertyType = this.propertyTypeTranslator.getPropertyType(node);

        boolean isJsonPrimitive = node.isJsonPrimitive();

        if (isJsonPrimitive) {
            this.storeNode(tree, nodeName, parentName, propertyType, documentId, level, timestamp,
                    node.getAsJsonPrimitive().getAsString());
        } else {
            this.storeNode(tree, nodeName, parentName, propertyType, documentId, level, timestamp, "");
        }

        if (node.isJsonObject()) {
            for (Entry<String, JsonElement> x : node.getAsJsonObject().entrySet()) {

                buildAndStore(entityType, nodeName, parser.parse(x.getValue().toString()), x.getKey(), (level + 1),
                        documentId, timestamp);
            }
        }
    }

    private void storeNode(Tree tree, String name, String parentName, PropertyType propertyType, Object documentId,
                           int level, int timestamp, Object value) {

        if (!this.isInBlackList(name)) {
            this.treeNodeManager.storeIfNotExists(tree.getRoot(), name, parentName, propertyType, level, documentId,
                    timestamp, value);
        }
    }

    private boolean isInBlackList(String name) {
        return blacklist.contains(name);
    }

}
