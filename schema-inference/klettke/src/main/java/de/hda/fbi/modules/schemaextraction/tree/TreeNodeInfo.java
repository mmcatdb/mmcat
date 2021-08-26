package de.hda.fbi.modules.schemaextraction.tree;

import java.io.Serializable;

/**
 * Contains the timestamp and the corresponding value of a attribute from an entity
 */
public class TreeNodeInfo implements Serializable {

    //id of the entity entry in the database
    private Object entityId;
    //the timestamp the tree node info is valid
    private int timestamp;
    //the value which the tree node info hold at this timestamp
    private Object value;

    public TreeNodeInfo(Object documentId, int timestamp, Object value) {
        this.entityId = documentId;
        this.timestamp = timestamp;
        this.value = value;
    }

    public Object getEntityId() {
        return entityId;
    }

    public void setEntityId(Object documentId) {
        this.entityId = documentId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
