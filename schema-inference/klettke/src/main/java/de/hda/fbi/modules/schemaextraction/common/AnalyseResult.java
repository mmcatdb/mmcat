package de.hda.fbi.modules.schemaextraction.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyseResult implements Serializable {

    private static final long serialVersionUID = -6877520049676646347L;
    private List<String> schemas;

    private Map<String, Integer> numberOfDifferentSchemasForEntityType = new HashMap<String, Integer>();

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public Map<String, Integer> getNumberOfDifferentSchemasForEntityType() {
        return numberOfDifferentSchemasForEntityType;
    }

    public void setNumberOfDifferentSchemasForEntityType(Map<String, Integer> numberOfDifferentSchemasForEntityType) {
        this.numberOfDifferentSchemasForEntityType = numberOfDifferentSchemasForEntityType;
    }
}
