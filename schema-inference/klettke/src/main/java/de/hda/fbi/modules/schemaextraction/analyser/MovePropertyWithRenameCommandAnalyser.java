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
 * Analyses all move with rename property commands
 *
 * @author daniel.mueller
 */
public class MovePropertyWithRenameCommandAnalyser implements CommandAnalyser {

    private TreeNodeInfoService treeNodeInfoService;

    public MovePropertyWithRenameCommandAnalyser(TreeNodeInfoService treeNodeInfoService) {
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

                        // Gibt es überhaupt gelöschte Knoten
                        if (deletedNodes.size() > 0) {
                            // Jeder gelöschte Knoten, der ungleich dem
                            // aktuellen ADD-Knoten ist
                            // und dessen Level und Datentyp gleich ist,
                            // ist ein potentieller MOVE WITH RENAME
                            for (TreeNode node : deletedNodes) {
                                if (node.getLevel() == addedNode.getLevel()
                                        && node.getPropertyType().equals(addedNode.getPropertyType())
                                        && !node.getName().equals(addedNode.getName())) {

                                    // Vorerst erzeugen wir ein Move und ein
                                    // RENAME
                                    // Command movePropertyCommand = new
                                    // CommandBuilder()
                                    // .from(addedNode.getName(),
                                    // node.getRoot().getName())
                                    // .to(addedNode.getName(),
                                    // addedNode.getRoot().getName()).moveProperty()
                                    // .dataType(DataType.STRING).build();
                                    //
                                    // Command renamePropertyCommand = new
                                    // CommandBuilder().renameProperty()
                                    // .from(node.getName(),
                                    // addedNode.getRoot().getName())
                                    // .toProperty(addedNode.getName()).dataType(DataType.STRING).build();

                                    // boolean isDeleted =
                                    // this.checkIfPropertyIsDeletedBetweenTimestamps(tree,
                                    // timestamp1, timestamp2,
                                    // relevantNode)(tree,
                                    // currentTimestamp, addedNode);
                                    //
                                    boolean hasProperties = hasPropertiesBefore(addedNode, currentTimestamp);

                                    if (hasProperties) {
                                        Command movePropertyCommandWithRename = new CommandBuilder()
                                                .from(node.getName(), node.getRoot().getName())
                                                .to(addedNode.getName(), addedNode.getRoot().getName()).moveProperty()
                                                .dataType(DataTypeTranslator.translate(addedNode.getPropertyType()))
                                                .build();

                                        CommandWithInfo cmdWithInfo = new CommandWithInfo(movePropertyCommandWithRename,
                                                node, currentTimestamp, timestampBefore, addedNode);
                                        commands.addElement(
                                                new CommandAnalyserResult(addedNode, currentTimestamp, cmdWithInfo));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return commands;
    }

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
