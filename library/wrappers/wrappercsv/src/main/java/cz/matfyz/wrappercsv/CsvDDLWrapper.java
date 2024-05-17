package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.ArrayList;
import java.util.List;

public class CsvDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;
    private List<String> properties = new ArrayList<>();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public boolean addSimpleProperty(String path, boolean required) {
        // The csv structure is flat, therefore, the path should be equal to the simple name of the property.
        properties.add(path);
        return true;
    }

    @Override public boolean addSimpleArrayProperty(String path, boolean required) {
      return false;
    }

    @Override public boolean addComplexProperty(String path, boolean required) {
        return false;
    }

    @Override public boolean addComplexArrayProperty(String path, boolean required) {
       return false;
    }

    @Override public CsvCommandStatement createDDLStatement() {
        final String headerLine = String.join(",", properties);

        return new CsvCommandStatement(headerLine);
    }

}
