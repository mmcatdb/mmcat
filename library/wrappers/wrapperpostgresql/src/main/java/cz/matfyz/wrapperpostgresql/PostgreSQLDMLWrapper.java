package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidNameException;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class PostgreSQLDMLWrapper implements AbstractDMLWrapper {

    @Override public void clear() {
        kindName = null;
        propertyValues.clear();
    }

    private String kindName = null;

    private record PropertyValue(String name, String value) {}

    private final List<PropertyValue> propertyValues = new ArrayList<>();

    @Override public void setKindName(String name) {
        if (!nameIsValid(name))
            throw InvalidNameException.kind(name);

        kindName = name;
    }

    @Override public void append(String name, @Nullable Object value) {
        if (!nameIsValid(name))
            throw InvalidNameException.property(name);

        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    private boolean nameIsValid(String name) {
        return name.matches("^[\\w.]+$");
    }

    @Override public StringStatement createDMLStatement() {
        if (kindName == null)
            throw InvalidNameException.kind(null);

        List<String> escapedNames = propertyValues.stream().map(propertyValue -> '"' + propertyValue.name + '"').toList();
        List<String> escapedValues = propertyValues.stream().map(propertyValue -> escapeString(propertyValue.value)).toList();

        String content = String.format("INSERT INTO \"%s\" (%s)\nVALUES (%s);", kindName, String.join(", ", escapedNames), String.join(", ", escapedValues));
        return new StringStatement(content);
    }

    private String escapeString(String input) {
        return input == null
            ? "NULL"
            : "'" + input.replace("'", "''") + "'";
    }

}
