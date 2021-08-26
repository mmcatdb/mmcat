package de.hda.fbi.modules.schemaextraction.analyser;

import de.hda.fbi.modules.schemaextraction.commands.Command;
import de.hda.fbi.modules.schemaextraction.commands.impl.CommandBuilder;
import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.common.DataTypeTranslator;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;

import java.util.Vector;

/**
 * Analyses all add property commands
 *
 * @author daniel.mueller
 */
public class AddPropertyCommandAnalyser implements CommandAnalyser {

	/**
	 * Analyses all add property commands
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
		Vector<CommandAnalyserResult> addPropertyCommands = new Vector<>();

		// Jeder hinzugefügte Knoten wird einzeln betrachet
		for (int addedNodeIndex = 0; addedNodeIndex < addedNodes.size(); addedNodeIndex++) {

			TreeNode addedNode = addedNodes.get(addedNodeIndex);

			// Alle neuen Knoten/Properties können als AddPropertyCommand
			// aufgefasst werden.
			// Nur wenn der Parent nicht null ist. Sonst wäre es der Root und
			// somit kein "richtiger" Knoten
			if (addedNode.getParent() != null) {

				Command addPropertyCommand = new CommandBuilder()
						.from(addedNode.getName(), addedNode.getRoot().getName())
						.dataType(DataTypeTranslator.translate(addedNode.getPropertyType())).addProperty().build();

				// Timestamp ist der currentTimestamp
				CommandWithInfo cmdWithInfo = new CommandWithInfo(addPropertyCommand, addedNode, currentTimestamp);

				addPropertyCommands.addElement(new CommandAnalyserResult(addedNode, currentTimestamp, cmdWithInfo));
			}
		}

		return addPropertyCommands;

	}

}
