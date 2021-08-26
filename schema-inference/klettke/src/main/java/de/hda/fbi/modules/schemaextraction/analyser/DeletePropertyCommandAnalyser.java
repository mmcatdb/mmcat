package de.hda.fbi.modules.schemaextraction.analyser;

import de.hda.fbi.modules.schemaextraction.commands.Command;
import de.hda.fbi.modules.schemaextraction.commands.impl.CommandBuilder;
import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.common.DataTypeTranslator;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;

import java.util.Vector;

/**
 * Analyses all delete property commands
 *
 * @author daniel.mueller
 */
public class DeletePropertyCommandAnalyser implements CommandAnalyser {

    public Vector<CommandAnalyserResult> analyse(Vector<TreeNode> addedNodes, Vector<TreeNode> deletedNodes,
                                                 Integer currentTimestamp, Integer lastTimestamp, SchemaExtractionUseCase useCase) {

        Vector<CommandAnalyserResult> deletePropertyCommands = new Vector<CommandAnalyserResult>();

        for (int deletedNodeIndex = 0; deletedNodeIndex < deletedNodes.size(); deletedNodeIndex++) {

            TreeNode deletedNode = deletedNodes.get(deletedNodeIndex);

            Command deletePropertyCommand = new CommandBuilder()
                    .from(deletedNode.getName(), deletedNode.getRoot().getName())
                    .dataType(DataTypeTranslator.translate(deletedNode.getPropertyType())).deleteProperty().build();

            CommandWithInfo cmdWithInfo = new CommandWithInfo(deletePropertyCommand, deletedNode, currentTimestamp,
                    lastTimestamp);

            deletePropertyCommands.addElement(new CommandAnalyserResult(deletedNode, currentTimestamp, cmdWithInfo));
        }

        return deletePropertyCommands;
    }

}
