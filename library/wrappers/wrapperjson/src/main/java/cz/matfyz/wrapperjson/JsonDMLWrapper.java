package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor.PropertyValue;
import cz.matfyz.core.exception.OtherException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A Data Manipulation Language (DML) wrapper for JSON that implements the {@link AbstractDMLWrapper} interface.
 * This class provides methods to build and manipulate DML statements for JSON data.
 */
public class JsonDMLWrapper implements AbstractDMLWrapper {

    @Override public void clear() {
        kindName = null;
        propertyValues.clear();
        constructor = new JsonDMLConstructor();
    }

    private String kindName = null;
    private final List<PropertyValue> propertyValues = new ArrayList<>();
    private JsonDMLConstructor constructor = new JsonDMLConstructor();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    /**
     * Appends a new field to the DML statement with the given name and value.
     * If the value is null, it is stored as a null string.
     *
     * @param name the name of the field.
     * @param value the value of the field; converted to a string.
     */
    @Override public void append(String name, @Nullable Object value) {
        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    /**
     * Creates a DML statement by constructing a JSON representation of the appended fields.
     */
    @Override public StringStatement createDMLStatement() {
        try {
            for (final PropertyValue propertyValue : propertyValues)
                constructor.addProperty(propertyValue);

            final String jsonContent = constructor.toPrettyString();
            return StringStatement.create(jsonContent);
        } catch (Exception e) {
            throw new OtherException(e);
        }
    }

    @Override public String joinStatements(Iterable<AbstractStatement> statements) {
        final String content = StreamSupport.stream(statements.spliterator(), false)
            .map(AbstractStatement::getContent)
            .collect(Collectors.joining(",\n"));

        return content.isEmpty()
            ? "[]"
            : "[\n" + content + "\n]";
    }

}
