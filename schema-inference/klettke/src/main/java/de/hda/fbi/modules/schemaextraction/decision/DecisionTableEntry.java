package de.hda.fbi.modules.schemaextraction.decision;

import java.io.Serializable;
import java.util.Vector;

public class DecisionTableEntry implements Serializable {

    private static final long serialVersionUID = -1306123278507425034L;

    private Integer timestamp;

    private boolean clarified;

    private String entityType;

    private String property;

    private Vector<DecisionCommand> decisionCommands;

    private int id;

    public DecisionTableEntry() {
    }

    public DecisionTableEntry(int id, Integer timestamp, boolean clarified, String entityType, String property, Vector<DecisionCommand> decisionCommands) {
        this.id = id;
        this.decisionCommands = decisionCommands;
        this.property = property;
        this.entityType = entityType;
        this.clarified = clarified;
        this.timestamp = timestamp;
    }

    public void removeDecisionCommand(DecisionCommand dCmd) {
        this.decisionCommands.remove(dCmd);
    }

    public DecisionCommand getCommand(int index) {
        for (DecisionCommand dCmd : decisionCommands) {
            if (dCmd.getIndex() == index) {
                return dCmd;
            }
        }

        return null;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Vector<DecisionCommand> getDecisionCommands() {
        return decisionCommands;
    }

    public void setDecisionCommands(Vector<DecisionCommand> decisionCommands) {
        this.decisionCommands = decisionCommands;
    }

    public boolean isClarified() {
        return clarified;
    }

    public void setClarified(boolean clarified) {
        this.clarified = clarified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
