package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor.PropertyValue;
import cz.matfyz.core.exception.OtherException;

import java.util.ArrayList;
import java.util.List;

public class JsonDMLWrapper implements AbstractDMLWrapper {

    private String kindName = null;
    private JsonDMLConstructor constructor = new JsonDMLConstructor();
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
    public JsonCommandStatement createDMLStatement() {
        try {
            for (PropertyValue propertyValue : propertyValues) {
                constructor.addProperty(propertyValue);
            }
            String jsonContent = constructor.toPrettyString();
            return new JsonCommandStatement(jsonContent);
        } catch (Exception e) {
            throw new OtherException(e);
        }
    }

    @Override
    public void clear() {
        kindName = null;
        propertyValues = new ArrayList<>();
        constructor = new JsonDMLConstructor(); // reset
    }
}
