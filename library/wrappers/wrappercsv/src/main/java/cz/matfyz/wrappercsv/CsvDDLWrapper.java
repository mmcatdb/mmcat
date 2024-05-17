package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class CsvDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;
    private Mapping mapping;

    public CsvDDLWrapper(Mapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void setKindName(String name) {
        kindName = name;
    }

    @Override
    public boolean isSchemaless() {
        return true;
    }

    @Override
    public boolean addSimpleProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override
    public boolean addSimpleArrayProperty(Set<String> names, boolean required) {
      return false;
    }

    @Override
    public boolean addComplexProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override
    public boolean addComplexArrayProperty(Set<String> names, boolean required) {
       return false;
    }

    @Override
    public CsvCommandStatement createDDLStatement() {
        ComplexProperty accessPath = mapping.accessPath();
        List<String> headers = accessPath.getSubpathNames();

        StringJoiner joiner = new StringJoiner(",");
        for (String header : headers) {
            joiner.add(header);
        }
        String headerLine = joiner.toString();
        return new CsvCommandStatement(headerLine);
    }
}
