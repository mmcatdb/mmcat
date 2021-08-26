package de.hda.fbi.modules.schemaextraction.commands;

import java.io.Serializable;

/**
 * Interface base for commands
 *
 * @author daniel.mueller
 */
public interface Command extends Serializable, Cloneable {

    String getEntityType();

    boolean isAddPropertyCommand();

    boolean isCopyPropertyCommand();

    boolean isCreateEntityTypeCommand();

    boolean isMovePropertyCommand();

    boolean isRenamePropertyCommand();

    boolean isDeletePropertyCommand();

    int getCommandVersion();

    void setCommandVersion(int version);

}
