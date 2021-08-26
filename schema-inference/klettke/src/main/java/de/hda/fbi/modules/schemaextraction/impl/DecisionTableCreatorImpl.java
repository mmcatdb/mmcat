package de.hda.fbi.modules.schemaextraction.impl;

import de.hda.fbi.modules.schemaextraction.DecisionTableCreator;
import de.hda.fbi.modules.schemaextraction.analyser.*;
import de.hda.fbi.modules.schemaextraction.commands.ComplexCommand;
import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.common.SchemaExtractionUseCase;
import de.hda.fbi.modules.schemaextraction.common.TimestampAndPropertyInfo;
import de.hda.fbi.modules.schemaextraction.configuration.SchemaExtractionConfiguration;
import de.hda.fbi.modules.schemaextraction.decision.DecisionCommand;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTableEntry;
import de.hda.fbi.modules.schemaextraction.tree.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

@Service
public class DecisionTableCreatorImpl implements DecisionTableCreator {

    private TreeHolder treeHolder;

    private TreeNodeInfoService treeNodeInfoService;

    public DecisionTableCreatorImpl() {

    }

    @Autowired
    public DecisionTableCreatorImpl(TreeNodeInfoService treeNodeInfoService, TreeNodeManager treeNodeManager,
                                    de.hda.fbi.modules.schemaextraction.EntityReader documentReader) {
        this.treeNodeInfoService = treeNodeInfoService;
        this.treeHolder = TreeHolder.getInstance();
    }

    @SuppressWarnings("Duplicates")
    public DecisionTable createDecisionTable(SchemaExtractionConfiguration extractionConfiguration) {
        // get the use case (e.g initial, incremental)
        SchemaExtractionUseCase useCase = extractionConfiguration.getUseCase();
        // get a list of all unique timestamps which will be used to iterate through
        Vector<Integer> allTimestampsSorted = this.treeHolder.getAllUniqueTimestampsSorted();

        Map<TimestampAndPropertyInfo, Vector<CommandWithInfo>> allCommandsWithDetectedConflicts = new HashMap<TimestampAndPropertyInfo, Vector<CommandWithInfo>>();

        // Mapping timestamp to documents
        Map<Integer, Set<Object>> timestampWithDocumentIds = new HashMap<>();

        Map<Integer, Set<Object>> timestampWithTimestamp = new HashMap<>();

        Map<String, Integer> lastSavedTimeStamps = new HashMap<>();

        int lastSavedTimestamp = 0;

        for (int i = 0; i < allTimestampsSorted.size(); i++) {

            int currentTimestamp = allTimestampsSorted.get(i);
            Integer lastTimestamp = 0;

            // Get the corresponding schema tree
            Tree tree = this.treeHolder.getTreeForTimestamp(currentTimestamp);

            // Get the last timestamp
            lastTimestamp = tree.getTimestampBefore(currentTimestamp);
            System.out.println("ts: " + currentTimestamp + " last timestamp: " + lastTimestamp);

            // check if there were any deleted nodes (attributes)
            Vector<TreeNode> deletedNodes = this.treeNodeInfoService.getDeletedNodes(tree.getRoot(), lastTimestamp, currentTimestamp);

            // check if any nodes (attributes) were added
            Vector<TreeNode> addedNodes = this.treeNodeInfoService.getAddedNodes(tree.getRoot(), lastTimestamp, currentTimestamp);

            // if there are any nodes that were added or deleted associate them with the current timestamp
            if (deletedNodes.size() > 0 || addedNodes.size() > 0) {
                timestampWithDocumentIds.put(currentTimestamp, getDocumentIds(tree.getRoot(), currentTimestamp));
                timestampWithTimestamp.put(currentTimestamp, getDocumentTs(tree.getRoot(), currentTimestamp));
                lastSavedTimestamp = currentTimestamp;
                lastSavedTimeStamps.put(tree.getName(), currentTimestamp);
            } else {

                // Es gibt keine Änderung, d.h. dieser Timestamp kann dem
                // Timestamp (vorher) zugeordnet werden
                lastSavedTimestamp = lastSavedTimeStamps.get(tree.getName());
                Set<Object> docIds = timestampWithDocumentIds.get(lastSavedTimestamp);

                if (docIds == null) {
                    timestampWithDocumentIds.put(currentTimestamp,
                            this.getDocumentIds(tree.getRoot(), currentTimestamp));
                    timestampWithTimestamp.put(currentTimestamp,
                            getDocumentTs(tree.getRoot(), currentTimestamp));
                } else {
                    docIds.addAll(getDocumentIds(tree.getRoot(), currentTimestamp));
                    timestampWithDocumentIds.remove(lastSavedTimestamp);
                    timestampWithDocumentIds.put(lastSavedTimestamp, docIds);

                    Set<Object> docts = timestampWithTimestamp.get(lastSavedTimestamp);
                    docts.addAll(getDocumentTs(tree.getRoot(), currentTimestamp));
                    timestampWithTimestamp.remove(lastSavedTimestamp);
                    timestampWithTimestamp.put(lastSavedTimestamp, docts);
                }
            }

            Vector<CommandAnalyserResult> analysedCommands = new Vector<>();

            analysedCommands.addAll(new CreateEntityTypeCommandAnalyser().analyse(addedNodes, deletedNodes,
                    currentTimestamp, lastSavedTimestamp, useCase));

            analysedCommands.addAll(new AddPropertyCommandAnalyser().analyse(addedNodes, deletedNodes, currentTimestamp,
                    lastSavedTimestamp, useCase));

            analysedCommands.addAll(new DeletePropertyCommandAnalyser().analyse(addedNodes, deletedNodes,
                    currentTimestamp, lastSavedTimestamp, useCase));

            analysedCommands.addAll(new RenamePropertyCommandAnalyser().analyse(addedNodes, deletedNodes,
                    currentTimestamp, lastSavedTimestamp, useCase));

            analysedCommands.addAll(new CopyPropertyCommandAnalyser(this.treeNodeInfoService).analyse(addedNodes,
                    deletedNodes, currentTimestamp, lastSavedTimestamp, useCase));

            analysedCommands.addAll(new MovePropertyCommandAnalyser(this.treeNodeInfoService).analyse(addedNodes,
                    deletedNodes, currentTimestamp, lastSavedTimestamp, useCase));

            analysedCommands.addAll(new MovePropertyWithRenameCommandAnalyser(this.treeNodeInfoService)
                    .analyse(addedNodes, deletedNodes, currentTimestamp, lastSavedTimestamp, useCase));

            // detect conflicts
            allCommandsWithDetectedConflicts = this.detectConflicts(analysedCommands, allCommandsWithDetectedConflicts);
        }

        // In Entscheidungstabelle überführen

        // Erzeuge eine neue Entscheidungstabelle
        DecisionTable decisionTable = new DecisionTable();

        // Alle DocumentIds mit zugehörigem Ts übergeben
        // decisionTable.setTimestampWithDocumentIds(timestampWithDocumentIds);
        // decisionTable.setTimestampWithTimestamp(timestampWithTimestamp);
        decisionTable.setTimestampWithDocumentIds(timestampWithTimestamp);
        Integer id = 1;
        for (Entry<TimestampAndPropertyInfo, Vector<CommandWithInfo>> entry : allCommandsWithDetectedConflicts
                .entrySet()) {

            // Neuer Eintrag
            DecisionTableEntry decisionTableEntry = new DecisionTableEntry();

            decisionTableEntry.setTimestamp(entry.getKey().getTimestamp());
            decisionTableEntry.setEntityType(entry.getKey().getEntityType());
            decisionTableEntry.setProperty(entry.getKey().getProperty());
            decisionTableEntry.setId(id);
            id++;
            Vector<CommandWithInfo> commands = entry.getValue();
            Vector<de.hda.fbi.modules.schemaextraction.decision.DecisionCommand> decisionCommands = new Vector<de.hda.fbi.modules.schemaextraction.decision.DecisionCommand>();

            Integer index = 0;
            for (CommandWithInfo cmdWithInfo : commands) {
                DecisionCommand dCmd = new DecisionCommand();
                dCmd.setCommand(cmdWithInfo);
                dCmd.setIndex(index++);

                if (cmdWithInfo.getCommand() instanceof ComplexCommand) {
                    Integer timestamp = cmdWithInfo.getTimestamp();

                    dCmd.setComplexCommand(true);

                    // Properties für die Where-Bedingung zur Verfügung stellen
                    Vector<TreeNode> nodesBeforeSourceProperty = this.treeNodeInfoService
                            .getNodesBeforeTimestamp(cmdWithInfo.getSourceProperty().getRoot(), timestamp);
                    Vector<TreeNode> nodesBeforeTargetProperty = this.treeNodeInfoService
                            .getNodesBeforeTimestamp(cmdWithInfo.getTargetProperty().getRoot(), timestamp);

                    dCmd.setSourceEntity(cmdWithInfo.getSourceProperty().getRoot().getName());
                    dCmd.setTargetEntity(cmdWithInfo.getTargetProperty().getRoot().getName());

                    List<String> sourceProperties = new ArrayList<String>();
                    List<String> targetProperties = new ArrayList<String>();

                    for (TreeNode node : nodesBeforeSourceProperty) {
                        sourceProperties.add(node.getName());
                    }

                    for (TreeNode node : nodesBeforeTargetProperty) {
                        targetProperties.add(node.getName());
                    }

                    dCmd.setTargetProperties(targetProperties);
                    dCmd.setSourceProperties(sourceProperties);
                }

                decisionCommands.addElement(dCmd);
            }

            if (entry.getValue().size() > 1) {
                decisionTableEntry.setClarified(false);
            } else {
                decisionTableEntry.setClarified(true);
            }

            decisionTableEntry.setDecisionCommands(decisionCommands);

            decisionTable.decisionEntries.addElement(decisionTableEntry);
        }

        // Wichtig! Tabelle sortieren, damit diese dem Benutzer richtig
        // angezeigt wird.
        decisionTable.sortAscending();

        return decisionTable;
    }

    private Map<TimestampAndPropertyInfo, Vector<CommandWithInfo>> detectConflicts(
            Vector<CommandAnalyserResult> analysedResults,
            Map<TimestampAndPropertyInfo, Vector<CommandWithInfo>> allCommandsWithDetectedConflicts) {

        for (CommandAnalyserResult result : analysedResults) {
            // gibt es diese Property und Timestamp bereits in der Liste?

            TimestampAndPropertyInfo key = this.containsKey(allCommandsWithDetectedConflicts,
                    result.getAffectedTreeNode().getName(), result.getTimestamp(),
                    result.getAffectedTreeNode().getRoot().getName());

            if (key != null) {
                // Konflikt entdeckt
                Vector<CommandWithInfo> commands = allCommandsWithDetectedConflicts.get(key);
                commands.add(result.getCommandWithInfo());
                allCommandsWithDetectedConflicts.remove(key);
                allCommandsWithDetectedConflicts.put(key, commands);
            } else {
                // Kein Konflikt
                Vector<CommandWithInfo> commands = new Vector<CommandWithInfo>();
                commands.add(result.getCommandWithInfo());
                TimestampAndPropertyInfo timestampAndPropertyInfo = new TimestampAndPropertyInfo();
                timestampAndPropertyInfo.setProperty(result.getAffectedTreeNode().getName());
                timestampAndPropertyInfo.setTimestamp(result.getTimestamp());
                timestampAndPropertyInfo.setEntityType(result.getAffectedTreeNode().getRoot().getName());
                allCommandsWithDetectedConflicts.put(timestampAndPropertyInfo, commands);
            }
        }

        return allCommandsWithDetectedConflicts;
    }

    private TimestampAndPropertyInfo containsKey(
            Map<TimestampAndPropertyInfo, Vector<CommandWithInfo>> commandsWithDetectedConflicts, String propertyName,
            int timestamp, String entityType) {

        for (Iterator<Map.Entry<TimestampAndPropertyInfo, Vector<CommandWithInfo>>> it = commandsWithDetectedConflicts
                .entrySet().iterator(); it.hasNext(); ) {

            Map.Entry<TimestampAndPropertyInfo, Vector<CommandWithInfo>> entry = it.next();

            if (entry.getKey().getProperty().equals(propertyName) && entry.getKey().getTimestamp().equals(timestamp)
                    && entry.getKey().getEntityType().equals(entityType)) {

                return entry.getKey();
            }

        }
        return null;
    }

    /**
     * Returns a set of entity ids which have attributes of the given timestamp. If no attributes could be found with
     * * the given timestamp an empty list is returned.
     *
     * @param sourceProperty  the root node of the tree representation.
     * @param sourceTimestamp the timestamp the attributes should have
     * @return a set of entity ids which have attributes at the given timestamp. If no attributes could be found with
     * the given timestamp an empty list is returned.
     */
    private Set<Object> getDocumentIds(TreeNode sourceProperty, int sourceTimestamp) {
        Set<Object> docIds = new HashSet<>();
        for (TreeNode node : sourceProperty.getChildren(true)) {
            for (TreeNodeInfo info : node.getTreeNodeInfos()) {
                if (info.getTimestamp() == sourceTimestamp) {
                    docIds.add(info.getEntityId());
                }
            }
        }
        return docIds;
    }

    /**
     * Returns a list with the same timestamp as the source timestamp!?
     * TODO pretty sure this is none sense except the number of entries of the same timestamp is important but than a simple integer could be returned
     *
     * @param sourceProperty
     * @param sourceTimestamp
     * @return
     */
    private Set<Object> getDocumentTs(TreeNode sourceProperty, int sourceTimestamp) {
        Set<Object> docIds = new HashSet<Object>();
        // iterate over TreeNodes (Attributes e.g. id)
        for (TreeNode node : sourceProperty.getChildren(true)) {
            // iterate over versions of attribute
            for (TreeNodeInfo info : node.getTreeNodeInfos()) {
                if (info.getTimestamp() == sourceTimestamp) {
                    docIds.add(info.getTimestamp());
                }
            }
        }
        return docIds;
    }
}
