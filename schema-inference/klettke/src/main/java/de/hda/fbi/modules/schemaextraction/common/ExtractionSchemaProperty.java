package de.hda.fbi.modules.schemaextraction.common;

import java.util.Vector;

public class ExtractionSchemaProperty {

    private String name;

    private String datatype;

    private Vector<ExtractionSchemaProperty> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Vector<ExtractionSchemaProperty> getProperties() {
        return properties;
    }

    public void setProperties(Vector<ExtractionSchemaProperty> properties) {
        this.properties = properties;
    }
}
