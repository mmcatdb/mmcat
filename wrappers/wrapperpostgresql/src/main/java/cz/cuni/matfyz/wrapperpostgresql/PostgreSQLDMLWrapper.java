package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.exception.WrapperException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLDMLWrapper implements AbstractDMLWrapper {

    private String kindName = null;
    
    private record PropertyValue(String name, String value) {}

    private List<PropertyValue> propertyValues = new ArrayList<>();
    
    @Override
    public void setKindName(String name) {
        if (!nameIsValid(name))
            throw new WrapperException("Kind name \"" + name + "\" doesn't match the required pattern /^[\\w]+$/.");

        kindName = name;
    }

    @Override
    public void append(String name, Object value) {
        if (!nameIsValid(name))
            throw new WrapperException("Property name \"" + name + "\" doesn't match the required pattern /^[\\w]+$/.");

        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    private boolean nameIsValid(String name) {
        return name.matches("^[\\w.]+$");
    }

    @Override
    public PostgreSQLStatement createDMLStatement() {
        if (kindName == null)
            throw new WrapperException("Kind name is null.");

        List<String> escapedNames = propertyValues.stream().map(propertyValue -> '"' + propertyValue.name + '"').toList();
        List<String> escapedValues = propertyValues.stream().map(propertyValue -> escapeString(propertyValue.value)).toList();
        
        String content = String.format("INSERT INTO \"%s\" (%s)\nVALUES (%s);", kindName, String.join(", ", escapedNames), String.join(", ", escapedValues));
        return new PostgreSQLStatement(content);
    }
    
    private String escapeString(String input) {
        return input == null
            ? "NULL"
            : "'" + input.replace("'", "''") + "'";
    }

    @Override
    public void clear() {
        kindName = null;
        propertyValues = new ArrayList<>();
    }

}
