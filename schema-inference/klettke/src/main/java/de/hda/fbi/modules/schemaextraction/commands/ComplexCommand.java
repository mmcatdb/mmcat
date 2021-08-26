package de.hda.fbi.modules.schemaextraction.commands;

import de.hda.fbi.modules.schemaextraction.commands.conditions.Condition;

/**
 * Complex command interface for move or copy commands
 *
 * @author daniel.mueller
 */
public interface ComplexCommand extends Command {

    String getTargetEntityType();

    String getTargetProperty();

    String getPropertyName();

    Condition getCondition();
}
