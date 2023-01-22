package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class MongoDBDMLWrapper implements AbstractDMLWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBDMLWrapper.class);

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
    public MongoDBCommandStatement createDMLStatement() {
        var constructor = new StatementConstructor();

        String content = "";
        try {
            for (var propertyValue : propertyValues)
                constructor.addProperty(propertyValue);

            content = String.format("db.%s.insert(%s);", kindName, constructor.toPrettyString());
        }
        catch (Exception exception) {
            LOGGER.error("Invalid MongoDB Statements resulted in a JSON error.", exception);
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

    @Override
    public void clear() {
        kindName = null;
        propertyValues = new ArrayList<>();
    }

    record PropertyValue(
        String name,
        String value
    ) {}

}

class StatementConstructor {

    private JSONObjectNode root = new JSONObjectNode();
    private Pattern arrayPattern = Pattern.compile("^([a-zA-Z0-9_-]+)\\[([0-9]+)\\]$");

    public String toString() {
        return root.object.toString();
    }

    public String toPrettyString() throws Exception {
        return root.object.toString(4);
    }

    void addProperty(MongoDBDMLWrapper.PropertyValue property) throws Exception {
        List<Key> keys = createKeys(property.name());

        add(root, keys, property.value());
    }

    private List<Key> createKeys(String path) {
        String[] split = path.split("/");
        List<Key> output = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
            Matcher arrayMatcher = arrayPattern.matcher(split[i]);
            if (arrayMatcher.find()) {
                String name = arrayMatcher.group(1);
                int index = Integer.parseInt(arrayMatcher.group(2));

                output.add(new Key(name));
                output.add(new Key(index));
            }
            else {
                output.add(new Key(split[i]));
            }
        }

        return output;
    }

    private void add(JSONNode parent, List<Key> path, String value) throws Exception {
        Key key = path.get(0);

        if (path.size() == 1) {
            parent.put(key, value);
            return;
        }

        path.remove(0);

        if (parent.has(key)) {
            JSONNode child = parent.get(key);
            add(child, path, value);
            return;
        }

        JSONNode child = create(path, value);
        parent.put(key, child);
    }

    private JSONNode create(List<Key> path, String value) throws Exception {
        Key key = path.get(0);
        JSONNode output = JSONNodeBase.fromKey(key);

        if (path.size() == 1) {
            output.put(key, value);
        }
        else {
            path.remove(0);
    
            JSONNode child = create(path, value);
            output.put(key, child);
        }

        return output;
    }

}

class Key {
    
    boolean isName;
    String name;
    int index;

    Key(String name) {
        this.name = name;
        this.isName = true;
    }

    Key(int index) {
        this.index = index;
        this.isName = false;
    }
}

interface JSONNode {

    Object toObject();

    void put(Key key, String value) throws Exception;

    void put(Key key, JSONNode child) throws Exception;

    boolean has(Key key);

    JSONNode get(Key key) throws Exception;

}

class JSONNodeBase {

    static JSONNode fromObject(Object object) {
        if (object instanceof JSONObject jsonObject)
            return new JSONObjectNode(jsonObject);
        else if (object instanceof JSONArray jsonArray)
            return new JSONArrayNode(jsonArray);
        else
            return null;
    }

    static JSONNode fromKey(Key key) {
        return key.isName ? new JSONObjectNode() : new JSONArrayNode();
    }

}

class JSONObjectNode implements JSONNode {

    public final JSONObject object;

    JSONObjectNode() {
        this.object = new JSONObject();
    }

    JSONObjectNode(JSONObject object) {
        this.object = object;
    }

    public Object toObject() {
        return object;
    }

    public void put(Key key, String value) throws Exception {
        object.put(key.name, value);
    }

    public void put(Key key, JSONNode child) throws Exception {
        object.put(key.name, child.toObject());
    }

    public boolean has(Key key) {
        return object.has(key.name);
    }

    public JSONNode get(Key key) throws Exception {
        return JSONNodeBase.fromObject(object.get(key.name));
    }
}

class JSONArrayNode implements JSONNode {

    public final JSONArray array;

    JSONArrayNode() {
        this.array = new JSONArray();
    }

    JSONArrayNode(JSONArray array) {
        this.array = array;
    }

    public Object toObject() {
        return array;
    }

    public void put(Key key, String value) throws Exception {
        array.put(key.index, value);
    }

    public void put(Key key, JSONNode child) throws Exception {
        array.put(key.index, child.toObject());
    }

    public boolean has(Key key) {
        return !array.isNull(key.index);
    }

    public JSONNode get(Key key) throws Exception {
        return JSONNodeBase.fromObject(array.get(key.index));
    }
}
