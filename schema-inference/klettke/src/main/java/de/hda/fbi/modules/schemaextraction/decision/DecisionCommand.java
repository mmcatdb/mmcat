package de.hda.fbi.modules.schemaextraction.decision;

import de.hda.fbi.modules.schemaextraction.common.CommandWithInfo;

import java.io.Serializable;
import java.util.List;

public class DecisionCommand implements Serializable {

    private static final long serialVersionUID = 2476029701512410469L;

    private CommandWithInfo command;

    private Integer index;

    private boolean isComplexCommand;

    private String sourceEntity;

    private String targetEntity;

    private List<String> sourceProperties;

    private List<String> targetProperties;

    public DecisionCommand() {
    }

    public DecisionCommand(CommandWithInfo command, Integer index, boolean isComplexCommand, String sourceEntity, String targetEntity,
                           List<String> sourceProperties, List<String> targetProperties) {
        this.command = command;
        this.index = index;
        this.isComplexCommand = isComplexCommand;
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.sourceProperties = sourceProperties;
        this.targetProperties = targetProperties;
    }

    public CommandWithInfo getCommand() {
        return command;
    }

    public void setCommand(CommandWithInfo command) {
        this.command = command;
    }

    public boolean isComplexCommand() {
        return isComplexCommand;
    }

    public void setComplexCommand(boolean isComplexCommand) {
        this.isComplexCommand = isComplexCommand;
    }

    public String getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(String sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public List<String> getSourceProperties() {
        return sourceProperties;
    }

    public void setSourceProperties(List<String> sourceProperties) {
        this.sourceProperties = sourceProperties;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public List<String> getTargetProperties() {
        return targetProperties;
    }

    public void setTargetProperties(List<String> targetProperties) {
        this.targetProperties = targetProperties;
    }
}
