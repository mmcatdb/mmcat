package de.hda.fbi.modules.schemaextraction.common;

import java.util.List;

public class LastExtractedSchema {

    private String entityTypeName;

    private List<LastExtractedSchemaProperty> properties;

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    public List<LastExtractedSchemaProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<LastExtractedSchemaProperty> properties) {
        this.properties = properties;
    }

}
