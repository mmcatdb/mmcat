package de.hda.fbi.modules.schemaextraction.analyser;

import de.hda.fbi.modules.schemaextraction.commands.Command;
import de.hda.fbi.modules.schemaextraction.commands.impl.CommandBuilder;
import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.common.DataTypeTranslator;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.tree.Tree;
import de.hda.fbi.modules.schemaextraction.tree.TreeHolder;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;
import de.hda.fbi.modules.schemaextraction.tree.TreeNodeInfoService;

import java.util.Vector;

/**
 * Analyses all move property commands
 *
 * @author daniel.mueller
 */
public class MovePropertyCommandAnalyser implements CommandAnalyser {

    private TreeNodeInfoService treeNodeInfoService;

    public MovePropertyCommandAnalyser(TreeNodeInfoService treeNodeInfoService) {
        this.treeNodeInfoService = treeNodeInfoService;
    }

    public Vector<CommandAnalyserResult> analyse(Vector<TreeNode> addedNodes, Vector<TreeNode> deletedNodes,
                                                 Integer currentTimestamp, Integer lastTimestamp, SchemaExtractionUseCase useCase) {

        return this.handleWithAddedNodes(addedNodes, currentTimestamp, useCase);
    }

    private Vector<CommandAnalyserResult> handleWithAddedNodes(Vector<TreeNode> addedNodes, Integer currentTimestamp,
                                                               SchemaExtractionUseCase useCase) {

        Vector<CommandAnalyserResult> commands = new Vector<CommandAnalyserResult>();

        for (int addedNodeIndex = 0; addedNodeIndex < addedNodes.size(); addedNodeIndex++) {

            TreeNode addedNode = addedNodes.get(addedNodeIndex);

            boolean nodeHandled = false;

            Vector<Tree> trees = TreeHolder.getInstance().getAllTrees();

            for (int i = 0; i < trees.size(); i++) {

                if (!nodeHandled) {

                    Tree tree = trees.get(i);

                    if (!tree.getName().equals(addedNode.getRoot().getName()) && addedNode.getParent() != null) {

                        Integer timestampBefore = tree.getTimestampBefore(currentTimestamp);
                        Integer timestampAfter = tree.getTimestampAfter(currentTimestamp);

                        Vector<TreeNode> deletedNodes = this.treeNodeInfoService.getDeletedNodes(tree.getRoot(),
                                timestampBefore, timestampAfter);

                        // Prüfe, ob in einer Timestamp davor diese Eigenschaft
                        // nicht mehr vorhanden war
                        // Wenn ja, dann hole aus dieser Eigenschaft die
                        // maximale Timestamp

                        Vector<TreeNode> allNodesOfEntityTypeBeforeCurrentTimestamp = this.treeNodeInfoService
                                .getNodesBeforeTimestamp(tree.getRoot(), currentTimestamp);

                        for (TreeNode node : allNodesOfEntityTypeBeforeCurrentTimestamp) {
                            // gibt es irgendwo diesen knoten
                            TreeNode relevantNode = node.nodeEquals(addedNode.getName(), addedNode.getPropertyType(),
                                    addedNode.getLevel());

                            if (relevantNode != null) {
                                // Hole die MaximumTimestamp,die aber kleiner
                                // als die übergebene ist
                                Integer maxTimestampLowerThanCurrentTimestamp = relevantNode
                                        .getMaximumTimestampLowerThan(currentTimestamp);

                                boolean isDeleted;
                                if (maxTimestampLowerThanCurrentTimestamp == null) {
                                    // Maximalen Timestamp gab es davor nicht
                                    // wurde also nicht gelöscht
                                    isDeleted = false;
                                } else {
                                    Integer timestampAfterMaxTimestamp = tree
                                            .getTimestampAfter(maxTimestampLowerThanCurrentTimestamp);

                                    isDeleted = this.checkIfPropertyIsDeletedBetweenTimestamps(tree,
                                            maxTimestampLowerThanCurrentTimestamp, timestampAfterMaxTimestamp,
                                            relevantNode);
                                }

                                // Da eine Bedingung durch den Anwender gesetzt
                                // werden muss
                                // macht es keinen Sinn ein CopyPropertyCommand
                                // zu machen, mit Bedingungen, ohne eine
                                // Property auf die die Bedingung abgesetzt
                                // werden kann

                                boolean hasProperties = hasPropertiesBefore(addedNode, currentTimestamp);

                                if (isDeleted && hasProperties) {

                                    // // Schauen ob es gute Properties für die
                                    // Condition gibt
                                    // List<PropertyWithValue>
                                    // propertyWithValue1 =
                                    // this.getValuesOfRoot(relevantNode.getRoot());
                                    // List<PropertyWithValue>
                                    // propertyWithValue2 =
                                    // this.getValuesOfRoot(addedNode.getRoot());
                                    //
                                    // Condition condition = null;
                                    //
                                    // if (useCase ==
                                    // SchemaExtractionUseCase.Initial){
                                    //
                                    // condition =
                                    // this.getCondition(propertyWithValue1,
                                    // propertyWithValue2);
                                    // }
                                    Command movePropertyCommand = new CommandBuilder()
                                            .from(relevantNode.getName(), relevantNode.getRoot().getName())
                                            .to(addedNode.getName(), addedNode.getRoot().getName()).moveProperty()
                                            .dataType(DataTypeTranslator.translate(addedNode.getPropertyType()))
                                            // .where(condition)
                                            .build();

                                    CommandWithInfo cmdWithInfo = new CommandWithInfo(movePropertyCommand, relevantNode,
                                            currentTimestamp, maxTimestampLowerThanCurrentTimestamp, addedNode);

                                    commands.addElement(
                                            new CommandAnalyserResult(addedNode, currentTimestamp, cmdWithInfo));
                                }
                            }
                        }
                    }
                }
            }
        }
        return commands;
    }

    // private Condition getCondition(List<PropertyWithValue>
    // propertyWithValue1,
    // List<PropertyWithValue> propertyWithValue2) {
    // for (PropertyWithValue entry1 : propertyWithValue1) {
    // for (PropertyWithValue entry2 : propertyWithValue2) {
    // if (entry1.getValue().equals(entry2.getValue())
    // && (!entry1.nodeEquals().getName().equals(entry2.nodeEquals().getName()))) {
    // return new JoinCondition(entry1.nodeEquals().getRoot().getName(),
    // entry1.nodeEquals().getName(),
    // entry2.nodeEquals().getRoot().getName(), entry2.nodeEquals().getName());
    // }
    // }
    // }
    //
    // return null;
    // }
    //
    // private List<PropertyWithValue> getValuesOfRoot(TreeNode rootNode) {
    //
    // List<PropertyWithValue> propertyNameWithValue = new
    // ArrayList<PropertyWithValue>();
    //
    // for (TreeNode node : rootNode.getChildren(true)) {
    // for (TreeNodeInfo info : node.getTreeNodeInfos()) {
    //
    // PropertyWithValue pWithV = new PropertyWithValue();
    // pWithV.setNode(node);
    // pWithV.setValue(info.getValue());
    // propertyNameWithValue.add(pWithV);
    // }
    // }
    //
    // return propertyNameWithValue;
    // }

    private boolean hasPropertiesBefore(TreeNode addedNode, Integer currentTimestamp) {
        // Tree of addedNode
        Tree treeOfAddedNode = TreeHolder.getInstance().getTree(addedNode.getRoot().getName());
        Vector<TreeNode> treeNodesBeforeAddedNode = this.treeNodeInfoService
                .getNodesBeforeTimestamp(treeOfAddedNode.getRoot(), currentTimestamp);

        boolean hasProperties = false;
        for (TreeNode tmpNode : treeNodesBeforeAddedNode) {

            // Wenn es einen Parent hat ist es ein Kind
            // und somit nicht der Entity-Typ an sich
            if (tmpNode.getParent() != null) {
                hasProperties = true;
            }
        }

        return hasProperties;
    }

    private boolean checkIfPropertyIsDeletedBetweenTimestamps(Tree tree, Integer timestamp1, Integer timestamp2,
                                                              TreeNode relevantNode) {

        if (timestamp2 == 0) {
            return false;
        }

        Vector<TreeNode> deletedNodes = this.treeNodeInfoService.getDeletedNodes(tree.getRoot(), timestamp1,
                timestamp2);

        for (TreeNode node : deletedNodes) {
            TreeNode deletedMatchedNode = node.nodeEquals(relevantNode.getName(), relevantNode.getPropertyType(),
                    relevantNode.getLevel());
            if (deletedMatchedNode != null) {
                return true;
            }

        }

        return false;
    }
}
