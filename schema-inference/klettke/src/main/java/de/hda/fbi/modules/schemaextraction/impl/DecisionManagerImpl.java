package de.hda.fbi.modules.schemaextraction.impl;

import de.hda.fbi.modules.schemaextraction.DecisionManager;
import de.hda.fbi.modules.schemaextraction.commands.ComplexCommand;
import de.hda.fbi.modules.schemaextraction.commands.conditions.JoinCondition;
import de.hda.fbi.modules.schemaextraction.commands.impl.AddPropertyCommand;
import de.hda.fbi.modules.schemaextraction.commands.impl.CopyPropertyCommand;
import de.hda.fbi.modules.schemaextraction.commands.impl.DeletePropertyCommand;
import de.hda.fbi.modules.schemaextraction.commands.impl.MovePropertyCommand;
import de.hda.fbi.modules.schemaextraction.commands.impl.RenamePropertyCommand;
import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;
import de.hda.fbi.modules.schemaextraction.decision.DecisionCommand;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTable;
import de.hda.fbi.modules.schemaextraction.decision.DecisionTableEntry;

import java.util.Vector;

import org.springframework.stereotype.Service;

@Service
public class DecisionManagerImpl implements DecisionManager {

    public DecisionTable clarifiyDecision(DecisionTable decisionTable, int decisionTableEntryId,
                                          int decisionCommandIndex, String sourceProperty, String targetProperty) {

        DecisionTableEntry decisionTableEntry = decisionTable.getDecisionTableEntry(decisionTableEntryId);

        if (decisionTableEntry == null || decisionTableEntry.isClarified()) {
            return decisionTable;
        }

        Vector<DecisionCommand> decisionCommands = new Vector<DecisionCommand>(
                decisionTableEntry.getDecisionCommands());

        for (DecisionCommand dCmd : decisionCommands) {
            if (dCmd.getIndex() == decisionCommandIndex) {

                // Handelt es sich um eine komplexe Änderungsoperation?
                // Bei Move und Property Commands werden dann die Conditions aus
                // der UI gesetzt.
                if (dCmd.isComplexCommand()) {

                    ComplexCommand command = (ComplexCommand) dCmd.getCommand().getCommand();

                    JoinCondition joinCondition = new JoinCondition(command.getEntityType(), sourceProperty,
                            command.getTargetEntityType(), targetProperty);

                    if (command.isMovePropertyCommand()) {
                        ((MovePropertyCommand) command).setCondition(joinCondition);
                    }

                    if (command.isCopyPropertyCommand()) {
                        ((CopyPropertyCommand) command).setCondition(joinCondition);
                    }
                }

                CommandWithInfo cmdWithInfo = dCmd.getCommand();

                // Wurde ein AddPropertyCommand gewählt, hat dies keine
                // Auswirkungen auf andere Änderungsoperationen
                // der Entscheidungstabelle. Klären und alle anderen Kommandes
                // des Tabelleneintrages löschen.
                if (cmdWithInfo.getCommand().isAddPropertyCommand()) {
                    // DO Nothing
                    decisionTableEntry.setClarified(true);
                } else if (cmdWithInfo.getCommand().isRenamePropertyCommand()) {

                    // Wurde ein Rename gewählt hat dies Auswirkungen auf andere
                    // Änderungsoperationen:
                    this.handleRenamePropertyCommandDecision(cmdWithInfo, decisionTable);
                    // handle RenameProperty Command
                    decisionTableEntry.setClarified(true);

                } else if (cmdWithInfo.getCommand().isMovePropertyCommand()) {

                    String entityType = cmdWithInfo.getCommand().getEntityType();
                    String targetEntityType = ((MovePropertyCommand) cmdWithInfo.getCommand()).getTargetEntityType();
                    String sourceEntityType = cmdWithInfo.getCommand().getEntityType();
                    String property = ((MovePropertyCommand) cmdWithInfo.getCommand()).getPropertyName();

                    Integer sourceTimestampOfCommand = cmdWithInfo.getSourceTimestampOfCommand();

                    Vector<DecisionCommand> possibleDecisionCommands = new Vector<DecisionCommand>();

                    for (DecisionTableEntry entry : decisionTable.decisionEntries) {
                        // Decision tmpDecision = entry.getDecision();

                        possibleDecisionCommands = new Vector<DecisionCommand>(entry.getDecisionCommands());

                        // Nachfolgende MovePropertyCommands
                        // Zwei MoveCommands mit gleichem SourceEntityType und
                        // SourceProperty
                        // aber unterschiedlichem Ziel
                        for (DecisionCommand tmpDecisionCommand : possibleDecisionCommands) {
                            if (tmpDecisionCommand.getCommand().getCommand().isMovePropertyCommand()
                                    && tmpDecisionCommand.getCommand().getSourceTimestampOfCommand()
                                    .equals(sourceTimestampOfCommand)) {
                                MovePropertyCommand relevantCommand = (MovePropertyCommand) tmpDecisionCommand
                                        .getCommand().getCommand();
                                if (!relevantCommand.getTargetEntityType().equals(targetEntityType)
                                        && (relevantCommand.getEntityType().equals(entityType)
                                        && relevantCommand.getPropertyName().equals(property))) {
                                    entry.removeDecisionCommand(tmpDecisionCommand);
                                }
                            }

                            // Auswirkung auf DeletePropertyCommands
                            if (tmpDecisionCommand.getCommand().getCommand().isDeletePropertyCommand()
                                    && tmpDecisionCommand.getCommand().getSourceTimestampOfCommand()
                                    .equals(sourceTimestampOfCommand)) {
                                DeletePropertyCommand relevantCommand = (DeletePropertyCommand) tmpDecisionCommand
                                        .getCommand().getCommand();
                                if (relevantCommand.getEntityType().equals(entityType)
                                        && relevantCommand.getPropertyName().equals(property)) {
                                    entry.removeDecisionCommand(tmpDecisionCommand);
                                }
                            }

                            // Auswirkung auf AddPropertyCommands
                            if (tmpDecisionCommand.getCommand().getCommand().isAddPropertyCommand()
                                    && tmpDecisionCommand.getCommand().getTimestamp()
                                    .equals(cmdWithInfo.getTimestamp())) {
                                AddPropertyCommand relevantCommand = (AddPropertyCommand) tmpDecisionCommand
                                        .getCommand().getCommand();
                                if (relevantCommand.getEntityType().equals(targetEntityType)
                                        && relevantCommand.getPropertyName().equals(property)) {
                                    entry.removeDecisionCommand(tmpDecisionCommand);
                                }
                            }

                            // Auswirkung auf RenamePropertyCommands
                            if (tmpDecisionCommand.getCommand().getCommand().isRenamePropertyCommand()
                                    && tmpDecisionCommand.getCommand().getSourceTimestampOfCommand()
                                    .equals(sourceTimestampOfCommand)) {
                                RenamePropertyCommand relevantCommand = (RenamePropertyCommand) tmpDecisionCommand
                                        .getCommand().getCommand();
                                if (relevantCommand.getEntityType().equals(sourceEntityType)
                                        && relevantCommand.getPropertyName().equals(property)) {
                                    entry.removeDecisionCommand(tmpDecisionCommand);
                                }
                            }

                            // Auswirkung auf CopyPropertyCommands
                            if (tmpDecisionCommand.getCommand().getCommand().isCopyPropertyCommand()
                                    && tmpDecisionCommand.getCommand().getSourceTimestampOfCommand()
                                    .equals(sourceTimestampOfCommand)) {
                                CopyPropertyCommand relevantCommand = (CopyPropertyCommand) tmpDecisionCommand
                                        .getCommand().getCommand();
                                if (relevantCommand.getEntityType().equals(sourceEntityType)
                                        && relevantCommand.getPropertyName().equals(property)) {
                                    entry.removeDecisionCommand(tmpDecisionCommand);
                                }
                            }
                        }
                    }
                }

                decisionTableEntry.setClarified(true);

            } else {
                decisionTableEntry.removeDecisionCommand(dCmd);
            }
        }

        for (DecisionTableEntry entry : decisionTable.decisionEntries) {

            if (entry.getDecisionCommands().size() == 1) {
                if (entry.isClarified() == false) {
                    entry.setClarified(true);
                }
            } else if (entry.getDecisionCommands().size() == 0) {
                entry.setClarified(true);
            }
        }

        return decisionTable;

    }

    private void handleRenamePropertyCommandDecision(CommandWithInfo cmdWithInfo, DecisionTable decisionTable) {

        Integer timestampOfCurrentDecisionCommand = cmdWithInfo.getTimestamp();

        // Aktuell gewählte Änderungsoperation
        RenamePropertyCommand currentRenamePropertyCommand = (RenamePropertyCommand) cmdWithInfo.getCommand();

        String entityType = currentRenamePropertyCommand.getEntityType();
        String oldPropertyName = currentRenamePropertyCommand.getPropertyName();
        String newPropertyName = currentRenamePropertyCommand.getTargetProperty();
        Vector<DecisionCommand> possibleDecisionCommands = new Vector<DecisionCommand>();

        for (DecisionTableEntry entry : decisionTable.decisionEntries) {
            // Decision tmpDecision = entry.getDecision();

            possibleDecisionCommands = new Vector<DecisionCommand>(entry.getDecisionCommands());

            for (DecisionCommand tmpDecisionCommand : possibleDecisionCommands) {

                boolean handled = false;

                // Ein RenamePropertyCommand hat Auswirkungen auf nachfolgende
                // DeletePropertyCommands und RenamePropertyCommands

                if (tmpDecisionCommand.getCommand().getCommand().isDeletePropertyCommand()
                        && tmpDecisionCommand.getCommand().getTimestamp().equals(timestampOfCurrentDecisionCommand)) {
                    DeletePropertyCommand relevantDeletePropertyCommand = (DeletePropertyCommand) tmpDecisionCommand
                            .getCommand().getCommand();

                    // Bei Rename Person.Id to Id2
                    // Gibt es ein nachfolgenden Command mit Delete Person.Id

                    // Der zweite Command verweist auf den Timestamp des ersten,
                    // da dieser dadurch entstanden ist
                    // Ist Entity-Typ und OldPropertyName gleich, kann diese
                    // gelöscht werden
                    if (relevantDeletePropertyCommand.getEntityType().equals(entityType)
                            && relevantDeletePropertyCommand.getPropertyName().equals(oldPropertyName)) {
                        entry.removeDecisionCommand(tmpDecisionCommand);
                        handled = true;
                    }
                }

                // Gibt es nun RenamePropertyCommands die auch die
                // oldPropertyName
                // gleich der aktuellen Entscheidung haben?
                if (handled == false && tmpDecisionCommand.getCommand().getCommand().isRenamePropertyCommand()
                        && tmpDecisionCommand.getCommand().getTimestamp().equals(timestampOfCurrentDecisionCommand)) {

                    RenamePropertyCommand relevantCommand = (RenamePropertyCommand) tmpDecisionCommand.getCommand()
                            .getCommand();

                    // Ein Rename kann nachfolgend nochmals entstehen, in der
                    // selben Version aber in einem anderen Eintrag.
                    if (relevantCommand.getEntityType().equals(entityType)
                            && (relevantCommand.getPropertyName().equals(oldPropertyName))
                            && !relevantCommand.getTargetProperty().equals(newPropertyName)) {
                        entry.removeDecisionCommand(tmpDecisionCommand);
                    }
                }
            }
        }
    }

}
