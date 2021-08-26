package de.hda.fbi.modules.schemaextraction.analyser;

import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;

import java.util.Vector;

/**
 * Basic interface for analyzing commands
 *
 * @author daniel.mueller
 */
public interface CommandAnalyser {

	/**
	 *
	 * @param addedNodes
	 * @param deletedNodes
	 * @param timestamp
	 * @param lastTimestamp
	 * @param useCase
	 * @return
	 */
	Vector<CommandAnalyserResult> analyse(Vector<TreeNode> addedNodes, Vector<TreeNode> deletedNodes, Integer timestamp,
			Integer lastTimestamp, SchemaExtractionUseCase useCase);
}
