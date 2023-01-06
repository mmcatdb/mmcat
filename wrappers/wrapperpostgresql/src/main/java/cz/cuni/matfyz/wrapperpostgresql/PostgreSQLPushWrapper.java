package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLPushWrapper implements AbstractPushWrapper {

    private String kindName = null;
    private List<PropertyValue> propertyValues = new ArrayList<>();
    
    @Override
    public void setKindName(String name) {
        kindName = name;
    }

    @Override
    public void append(String name, Object value) {
        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    @Override
    public PostgreSQLDMLStatement createDMLStatement() {
        if (!nameIsValid(kindName))
            // This should not happen.
            throw new UnsupportedOperationException("Kind name \"" + kindName + "\" doesn't meet required pattern /^[\\w]+$/.");

        List<String> names = propertyValues.stream().map(propertyValue -> propertyValue.name).toList();
        for (var name : names)
            if (!nameIsValid(name))
                // Neither this.
                throw new UnsupportedOperationException("Property name \"" + name + "\" doesn't meet required pattern /^[\\w]+$/.");
            
        List<String> escapedNames = names.stream().map(name -> '"' + name + '"').toList();
        List<String> escapedValues = propertyValues.stream().map(propertyValue -> escapeString(propertyValue.value)).toList();
        
        String content = String.format("INSERT INTO \"%s\" (%s)\nVALUES (%s);", kindName, String.join(", ", escapedNames), String.join(", ", escapedValues));
        return new PostgreSQLDMLStatement(content);
    }

    private boolean nameIsValid(String name) {
        return name.matches("^[\\w.]+$");
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

    private record PropertyValue(
        String name,
        String value
    ) {}

}
