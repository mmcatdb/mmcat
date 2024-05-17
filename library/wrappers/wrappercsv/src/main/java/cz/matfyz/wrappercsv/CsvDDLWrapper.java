package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CsvDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;
    private List<String> properties = new ArrayList<>();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public boolean addSimpleProperty(Set<String> names, boolean required) {
        properties.addAll(names);
        return true;
    }

    @Override public boolean addSimpleArrayProperty(Set<String> names, boolean required) {
      return false;
    }

    @Override public boolean addComplexProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override public boolean addComplexArrayProperty(Set<String> names, boolean required) {
       return false;
    }

    @Override public CsvCommandStatement createDDLStatement() {
        final String headerLine = String.join(",", properties);

        return new CsvCommandStatement(headerLine);
    }

}
