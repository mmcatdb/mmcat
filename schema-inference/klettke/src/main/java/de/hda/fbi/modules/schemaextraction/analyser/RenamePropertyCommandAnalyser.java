package de.hda.fbi.modules.schemaextraction.analyser;

import de.hda.fbi.modules.schemaextraction.commands.Command;
import de.hda.fbi.modules.schemaextraction.commands.impl.CommandBuilder;
import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.common.DataTypeTranslator;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;

import java.util.Vector;

/**
 * Analyses all rename property commands
 *
 * @author daniel.mueller
 */
public class RenamePropertyCommandAnalyser implements CommandAnalyser {

    public Vector<CommandAnalyserResult> analyse(Vector<TreeNode> addedNodes, Vector<TreeNode> deletedNodes,
                                                 Integer currentTimestamp, Integer lastTimestamp, SchemaExtractionUseCase useCase) {

        // Es muss mindestens ein neuer Knoten und ein gelöschter Knoten
        // vorhanden sein.
        if (addedNodes.size() == 0 || deletedNodes.size() == 0) {
            return new Vector<CommandAnalyserResult>();
        }

        Vector<CommandAnalyserResult> renamePropertyCommands = new Vector<CommandAnalyserResult>();

        for (int addedNodeIndex = 0; addedNodeIndex < addedNodes.size(); addedNodeIndex++) {

            TreeNode addedNode = addedNodes.get(addedNodeIndex);

            for (int deletedNodeIndex = 0; deletedNodeIndex < deletedNodes.size(); deletedNodeIndex++) {

                TreeNode deletedNode = deletedNodes.get(deletedNodeIndex);

                // Nur wenn Level und Datentyp übereinstimmen (Name nicht, da
                // RENAME)
                if (addedNode.getLevel() == deletedNode.getLevel()
                        && addedNode.getPropertyType().equals(deletedNode.getPropertyType())) {

                    Command renamePropertyCommand = new CommandBuilder().renameProperty()
                            .from(deletedNodes.get(deletedNodeIndex).getName(),
                                    deletedNodes.get(deletedNodeIndex).getRoot().getName())
                            .toProperty(addedNodes.get(addedNodeIndex).getName())
                            .dataType(DataTypeTranslator.translate(deletedNode.getPropertyType())).build();

                    CommandWithInfo cmdWithInfo = new CommandWithInfo(renamePropertyCommand,
                            addedNodes.get(addedNodeIndex), currentTimestamp, lastTimestamp);

                    renamePropertyCommands.addElement(
                            new CommandAnalyserResult(addedNodes.get(addedNodeIndex), currentTimestamp, cmdWithInfo));
                }
            }
        }

        return renamePropertyCommands;

    }

}
