package de.hda.fbi.modules.schemaextraction.common;

import de.hda.fbi.modules.schemaextraction.commands.Command;
import de.hda.fbi.modules.schemaextraction.tree.TreeNode;

import java.io.Serializable;

public class CommandWithInfo implements Serializable {

    private static final long serialVersionUID = -1878361016495228308L;

    private de.hda.fbi.modules.schemaextraction.commands.Command command;

    private TreeNode sourceProperty;

    private TreeNode targetProperty;

    private Integer timestamp;

    private Integer sourceTimestampOfCommand;

    private int globalTimestamp;

    public CommandWithInfo(Command command, TreeNode sourceProperty, int timestamp) {
        this(command, sourceProperty, timestamp, -1);
    }

    public CommandWithInfo(Command command, TreeNode sourceProperty, int timestamp, int sourceTimestampOfCommand) {
        this(command, sourceProperty, timestamp, sourceTimestampOfCommand, null);
    }

    public CommandWithInfo(Command command, TreeNode sourceProperty, Integer timestamp, int sourceTimestampOfCommand,
                           TreeNode targetProperty) {
        this.command = command;
        this.sourceProperty = sourceProperty;
        this.timestamp = timestamp;
        this.globalTimestamp = GlobalTimestampProvider.getInstance().getNextTimestamp();
        this.targetProperty = targetProperty;
        this.sourceTimestampOfCommand = sourceTimestampOfCommand;

    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public TreeNode getSourceProperty() {
        return sourceProperty;
    }

    public void setSourceProperty(TreeNode sourceProperty) {
        this.sourceProperty = sourceProperty;
    }

    public TreeNode getTargetProperty() {
        return targetProperty;
    }

    public void setTargetProperty(TreeNode targetProperty) {
        this.targetProperty = targetProperty;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getGlobalTimestamp() {
        return globalTimestamp;
    }

    public void setGlobalTimestamp(Integer globalTimestamp) {
        this.globalTimestamp = globalTimestamp;
    }

    public Integer getSourceTimestampOfCommand() {
        return sourceTimestampOfCommand;
    }

    public void setSourceTimestampOfCommand(Integer sourceTimestampOfCommand) {
        this.sourceTimestampOfCommand = sourceTimestampOfCommand;
    }

}
