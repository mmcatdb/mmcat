package de.hda.fbi.modules.schemaextraction.analyser;

import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;

/**
 * @author daniel.mueller
 */
public final class CommandAnalyserResult {

	private TreeNode affectedTreeNode;

	private int timestamp;

	private CommandWithInfo commandWithInfo;

	public CommandAnalyserResult(TreeNode affectedTreeNode, Integer timestamp, CommandWithInfo commandWithInfo) {
		this.setAffectedTreeNode(affectedTreeNode);
		this.setTimestamp(timestamp);
		this.setCommandWithInfo(commandWithInfo);
	}

	public CommandWithInfo getCommandWithInfo() {
		return commandWithInfo;
	}

	public void setCommandWithInfo(CommandWithInfo commandWithInfo) {
		this.commandWithInfo = commandWithInfo;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public TreeNode getAffectedTreeNode() {
		return affectedTreeNode;
	}

	public void setAffectedTreeNode(TreeNode affectedTreeNode) {
		this.affectedTreeNode = affectedTreeNode;
	}

}
