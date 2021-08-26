package de.hda.fbi.modules.schemaextraction.common;

import java.util.Vector;

public class TimestampAndPropertyInfo {

    private Integer timestamp;

    private String property;

    private String entityType;

    private Vector<String> documentIds;

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

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Vector<String> getDocumentIds() {
        return documentIds;
    }

    public void setDocumentIds(Vector<String> documentIds) {
        this.documentIds = documentIds;
    }
}
