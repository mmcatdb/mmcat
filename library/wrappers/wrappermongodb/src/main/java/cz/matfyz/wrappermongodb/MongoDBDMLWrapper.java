package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor;
import cz.matfyz.abstractwrappers.utils.JsonDMLConstructor.PropertyValue;
import cz.matfyz.core.exception.OtherException;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MongoDBDMLWrapper implements AbstractDMLWrapper {

    @Override public void clear() {
        kindName = null;
        propertyValues.clear();
    }

    private String kindName = null;
    private final List<PropertyValue> propertyValues = new ArrayList<>();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public void append(String name, @Nullable Object value) {
        String stringValue = value == null ? null : value.toString();
        propertyValues.add(new PropertyValue(name, stringValue));
    }

    @Override public MongoDBCommandStatement createDMLStatement() {
        var constructor = new JsonDMLConstructor();

        String content = "";
        try {
            for (var propertyValue : propertyValues)
                constructor.addProperty(propertyValue);

            content = String.format("db.%s.insert(%s);", kindName, constructor.toPrettyString());
        }
        catch (Exception e) {
            throw new OtherException(e);
        }

        final var command = new BsonDocument();
        command.append("insert", new BsonString(kindName));
        command.append("documents", new BsonArray(List.of(BsonDocument.parse(constructor.toString()))));

        return new MongoDBCommandStatement(content, command);
    }

    /*
    private String escapeString(String input) {
        return "\"" + input.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
    */

}
