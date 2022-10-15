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
        List<String> names = propertyValues.stream().map(propertyValue -> propertyValue.name).toList();
        List<String> values = propertyValues.stream().map(propertyValue -> escapeString(propertyValue.value)).toList();
        
        String content = String.format("INSERT INTO %s (%s)\nVALUES (%s);", kindName, String.join(", ", names), String.join(", ", values));
        return new PostgreSQLDMLStatement(content);
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

class PropertyValue {
    
    public String name;
    public String value;
    
    public PropertyValue(String name, String value) {
        this.name = name;
        this.value = value;
    }
}