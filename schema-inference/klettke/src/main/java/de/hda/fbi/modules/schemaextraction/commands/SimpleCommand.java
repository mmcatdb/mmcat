package de.hda.fbi.modules.schemaextraction.commands;

import de.hda.fbi.modules.schemaextraction.commands.conditions.Condition;

/**
 * simple command interface for add, delete, rename commands
 *
 * @author daniel.mueller
 */
public interface SimpleCommand extends Command {

    String getEntityType();

    Condition getCondition();
}
