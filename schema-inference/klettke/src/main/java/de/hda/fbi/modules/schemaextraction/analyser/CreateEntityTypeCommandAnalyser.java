package de.hda.fbi.modules.schemaextraction.analyser;

import de.hda.fbi.modules.schemaextraction.commands.Command;
import de.hda.fbi.modules.schemaextraction.commands.impl.CommandBuilder;
import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;

import java.util.Vector;

/**
 * Analyses all create entity type commands
 *
 * @author daniel.mueller
 */
public class CreateEntityTypeCommandAnalyser implements CommandAnalyser {

	/**
	 *
	 * @param addedNodes
	 * @param deletedNodes
	 * @param currentTimestamp
	 * @param lastTimestamp
	 * @param useCase
	 * @return
	 */
	@Override
    public Vector<CommandAnalyserResult> analyse(Vector<TreeNode> addedNodes, Vector<TreeNode> deletedNodes,
                                                 Integer currentTimestamp, Integer lastTimestamp, SchemaExtractionUseCase useCase) {

        Vector<CommandAnalyserResult> createEntityTypeCommands = new Vector<>();

        for (TreeNode node : addedNodes) {
            if (node.getParent() == null) {

                Command command = new CommandBuilder().createEntity(node.getRoot().getName()).build();

                CommandWithInfo cmdWithInfo = new CommandWithInfo(command, node, currentTimestamp);

                createEntityTypeCommands.addElement(new CommandAnalyserResult(node, currentTimestamp, cmdWithInfo));
            }
        }

        return createEntityTypeCommands;
    }
}
