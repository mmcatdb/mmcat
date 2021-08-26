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
 * Analyses all copy property commands
 *
 * @author daniel.mueller
 */
public class CopyPropertyCommandAnalyser implements CommandAnalyser {

	private final TreeNodeInfoService treeNodeInfoService;

	public CopyPropertyCommandAnalyser(TreeNodeInfoService treeNodeInfoService) {
		this.treeNodeInfoService = treeNodeInfoService;
	}

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

		return this.handleWithAddedNodes(addedNodes, currentTimestamp, useCase);

	}

	private Vector<CommandAnalyserResult> handleWithAddedNodes(Vector<TreeNode> addedNodes, Integer currentTimestamp,
			SchemaExtractionUseCase useCase) {

		Vector<CommandAnalyserResult> commands = new Vector<>();

		for (int addedNodeIndex = 0; addedNodeIndex < addedNodes.size(); addedNodeIndex++) {

			TreeNode addedNode = addedNodes.get(addedNodeIndex);

			boolean nodeHandled = false;

			Vector<Tree> trees = TreeHolder.getInstance().getAllTrees();

			for (int i = 0; i < trees.size(); i++) {

				if (!nodeHandled) {

					Tree tree = trees.get(i);

					if (!tree.getName().equals(addedNode.getRoot().getName()) && addedNode.getParent() != null) {

						// Existiert in der Timestamp vorher des anderen Baumes
						// diese Eigenschaft überhaupt???
						TreeNode lastSourceTreeNodeBefore = null;

						Integer timestampBefore = tree.getTimestampBefore(currentTimestamp);
						Vector<TreeNode> treeNodes = this.treeNodeInfoService.getNodesWithTimestamp(tree.getRoot(), timestampBefore);

						for (TreeNode node : treeNodes) {
							if (node.nodeEquals(addedNode.getName(), addedNode.getPropertyType(),
									addedNode.getLevel()) != null) {
								lastSourceTreeNodeBefore = node;
							}
						}

						// Nur wenn der Knoten vorher existiert hat, kann hier
						// ein CopyPropertyCommand erzeugt werden.
						if (lastSourceTreeNodeBefore != null) {

							// Da eine Bedingung durch den Anwender gesetzt
							// werden muss
							// macht es keinen Sinn ein CopyPropertyCommand zu
							// machen, mit Bedingungen, ohne eine Property auf
							// die die Bedingung abgesetzt werden kann
							// Tree of addedNode
							Tree treeOfAddedNode = TreeHolder.getInstance().getTree(addedNode.getRoot().getName());
							Vector<TreeNode> treeNodesBeforeAddedNode = this.treeNodeInfoService
									.getNodesBeforeTimestamp(treeOfAddedNode.getRoot(), currentTimestamp);

							boolean hasProperties = false;

							for (TreeNode node : treeNodesBeforeAddedNode) {

								// Wenn es einen Parent hat ist es ein Kind und
								// somit nicht der Entity-Typ an sich
								if (node.getParent() != null) {
									hasProperties = true;
								}
							}

							boolean canCreateCopyProperyCommand = false;
							Integer timestampAfter = tree.getTimestampAfter(currentTimestamp);
							if (tree.hasTimestamp(timestampAfter)) {

								Vector<TreeNode> nodesAfter = this.treeNodeInfoService.getNodesWithTimestamp(tree.getRoot(),
										timestampAfter);

								if (tree.hasTimestamp(timestampAfter)) {
									// Existiert der Knoten auch noch in der
									// Timestamp danach?
									// Nur wenn es auch wirklich noch Entities
									// danach gibt
									for (TreeNode node : nodesAfter) {
										if (node.nodeEquals(addedNode.getName(), addedNode.getPropertyType(),
												addedNode.getLevel()) != null) {
											canCreateCopyProperyCommand = true;
										}

									}
								}
							} else {
								canCreateCopyProperyCommand = true;
							}

							if (hasProperties && canCreateCopyProperyCommand) {

								// // Schauen ob es gute Properties für die
								// Condition gibt
								// List<PropertyWithValue> propertyWithValue1 =
								// this.getValuesOfRoot(lastTreeNodeBefore.getRoot());
								// List<PropertyWithValue> propertyWithValue2 =
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
								Command copyPropertyCommand = new CommandBuilder()
										.from(lastSourceTreeNodeBefore.getName(),
												lastSourceTreeNodeBefore.getRoot().getName())
										.to(addedNode.getName(), addedNode.getRoot().getName()).copyProperty()
										.dataType(DataTypeTranslator.translate(addedNode.getPropertyType())).build();
								// .where(condition).build();

								// timestampBefore = Aus welchem Timestamp kommt
								// die CopyProperty-Änderungsoperation
								// currentTimestamp = was ist der aktuelle
								// Timestamp
								CommandWithInfo cmdWithInfo = new CommandWithInfo(copyPropertyCommand,
										lastSourceTreeNodeBefore, currentTimestamp, timestampBefore, addedNode);

								commands.addElement(
										new CommandAnalyserResult(addedNode, currentTimestamp, cmdWithInfo));
							}
						}
					}
				}
			}
		}

		return commands;
	}

	// private Condition getCondition(List<PropertyWithValue>
	// propertyWithValue1, List<PropertyWithValue> propertyWithValue2){
	// for (PropertyWithValue entry1 : propertyWithValue1){
	// for (PropertyWithValue entry2 : propertyWithValue2){
	// if (entry1.getValue().equals(entry2.getValue()) &&
	// (!entry1.nodeEquals().getName().equals(entry2.nodeEquals().getName()))) {
	// return new JoinCondition(entry1.nodeEquals().getRoot().getName(),
	// entry1.nodeEquals().getName(), entry2.nodeEquals().getRoot().getName(),
	// entry2.nodeEquals().getName());
	// }
	// }
	// }
	//
	// return null;
	// }
	//
	// private List<PropertyWithValue> getValuesOfRoot(TreeNode rootNode){
	//
	// List<PropertyWithValue> propertyNameWithValue = new
	// ArrayList<PropertyWithValue>();
	//
	// for (TreeNode node : rootNode.getChildren(true)){
	// for (TreeNodeInfo info : node.getTreeNodeInfos()){
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
}
