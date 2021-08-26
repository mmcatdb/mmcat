package de.hda.fbi.modules.schemaextraction.common;

public class LastExtractedSchemaProperty {

    private String propertyName;

    private DataType dataType;

    public LastExtractedSchemaProperty(String propertyName, DataType dataType) {
        this.setPropertyName(propertyName);
        this.setDataType(dataType);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
}
