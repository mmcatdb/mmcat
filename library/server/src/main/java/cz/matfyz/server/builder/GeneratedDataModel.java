package cz.matfyz.server.builder;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.server.entity.Id;
import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;

import java.util.List;
import java.util.ArrayList;

public class GeneratedDataModel {

    private String name;
    private Id id;
    private DatasourceType outputType;
    private List<ComplexProperty> accessPaths;

    public GeneratedDataModel(String name, Id id, DatasourceType outputType) {
        this.name = name;
        this.id = id;
        this.outputType = outputType;
        this.accessPaths = new ArrayList<>();
    }

    public void addAccessPath(ComplexProperty complexProperty) {
        if (complexProperty != null) {
            this.accessPaths.add(complexProperty);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("\t\"name\": \"").append(name).append("\",\n");
        builder.append("\t\"id\": ").append(id).append(",\n");
        builder.append("\t\"outputType\": \"").append(outputType).append("\",\n");
        builder.append("\t\"accessPaths\": [\n");

        for (ComplexProperty complexProperty : accessPaths) {
            builder.append("\t\t").append(complexProperty.toString().replace("\n", "\n\t\t")).append(",\n");
        }

        // Remove the last comma and newline
        if (!accessPaths.isEmpty()) {
            builder.setLength(builder.length() - 2);
            builder.append("\n");
        }

        builder.append("\t]\n");
        builder.append("}");
        return builder.toString();
    }
}

