package cz.cuni.matfyz.abstractwrappers.other;

import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractStatement;
import cz.cuni.matfyz.abstractwrappers.utils.JsonDMLConstructor;
import cz.cuni.matfyz.abstractwrappers.utils.JsonDMLConstructor.PropertyValue;
import cz.cuni.matfyz.core.exception.OtherException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class JsonDMLWrapper implements AbstractDMLWrapper {

    private List<PropertyValue> propertyValues = new ArrayList<>();
    
    @Override
    public void setKindName(String name) {
        // This method is intentionally empty
    }

    @Override
    public void append(String name, Object value) {
        final String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    static record JsonStatement(
        String content
    ) implements AbstractStatement {
        @Override
        public String getContent() {
            return this.content;
        }
    }

    @Override
    public JsonStatement createDMLStatement() {
        final var constructor = new JsonDMLConstructor();

        try {
            for (var propertyValue : propertyValues)
                constructor.addProperty(propertyValue);

            return new JsonStatement(constructor.toPrettyString());
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    @Override
    public void clear() {
        propertyValues = new ArrayList<>();
    }

}
