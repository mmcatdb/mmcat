package cz.matfyz.server.builder;

import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.server.entity.Id;
import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the overall model of a Schema Category, based on a Logical Model
 */
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
        builder.append("{\n")
               .append(indent("\"name\": \"" + name + "\",\n", 1))
               .append(indent("\"id\": " + id + ",\n", 1))
               .append(indent("\"outputType\": \"" + outputType + "\",\n", 1))
               .append(indent("\"accessPaths\": [\n", 1));

        for (ComplexProperty complexProperty : accessPaths) {
            builder.append(indent(convertComplexPropertyToJson(complexProperty), 2)).append(",\n");
        }

        if (!accessPaths.isEmpty()) {
            builder.setLength(builder.length() - 2);
            builder.append("\n");
        }

        builder.append(indent("]\n", 1))
               .append("}");
        return builder.toString();
    }

    private String convertComplexPropertyToJson(ComplexProperty complexProperty) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n")
                   .append(indent("\"name\": \"" + complexProperty.name() + "\",\n", 1))
                   .append(indent("\"signature\": \"" + complexProperty.signature() + "\",\n", 1))
                   .append(indent("\"subpaths\": [\n", 1));

        for (AccessPath subpath : complexProperty.subpaths()) {
            if (subpath instanceof ComplexProperty) {
                jsonBuilder.append(indent(convertComplexPropertyToJson((ComplexProperty) subpath), 2)).append(",\n");
            } else {
                jsonBuilder.append(indent(convertSimplePropertyToJson(subpath), 2)).append(",\n");
            }
        }

        if (!complexProperty.subpaths().isEmpty()) {
            jsonBuilder.setLength(jsonBuilder.length() - 2);
            jsonBuilder.append("\n");
        }

        jsonBuilder.append(indent("]\n", 1))
                   .append("}");
        return jsonBuilder.toString();
    }

    private String convertSimplePropertyToJson(AccessPath accessPath) {
        return "{\n" +
               indent("\"name\": \"" + accessPath.name() + "\",\n", 1) +
               indent("\"signature\": \"" + accessPath.signature() + "\",\n", 1) +
               indent("\"subpaths\": []\n", 1) +
               "}";
    }

    private String indent(String text, int level) {
        String indent = " ".repeat(level * 4);
        return text.replaceAll("(?m)^", indent);
    }
}
