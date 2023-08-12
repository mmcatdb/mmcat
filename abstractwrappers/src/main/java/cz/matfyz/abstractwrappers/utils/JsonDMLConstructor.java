package cz.matfyz.abstractwrappers.utils;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonDMLConstructor {

    private JsonObjectNode root = new JsonObjectNode();
    private Pattern arrayPattern = Pattern.compile("^([a-zA-Z0-9_-]+)\\[([0-9]+)\\]$");

    public String toString() {
        return root.object.toString();
    }

    public String toPrettyString() throws JSONException {
        return root.object.toString(4);
    }

    public static record PropertyValue(String name, String value) {}

    public void addProperty(PropertyValue property) throws Exception {
        List<Key> keys = createKeys(property.name());

        add(root, keys, property.value());
    }

    private List<Key> createKeys(String path) {
        String[] split = path.split(AbstractDDLWrapper.PATH_SEPARATOR);
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

    private void add(JsonNode parent, List<Key> path, String value) throws Exception {
        Key key = path.get(0);

        if (path.size() == 1) {
            parent.put(key, value);
            return;
        }

        path.remove(0);

        if (parent.has(key)) {
            JsonNode child = parent.get(key);
            add(child, path, value);
            return;
        }

        JsonNode child = create(path, value);
        parent.put(key, child);
    }

    private JsonNode create(List<Key> path, String value) throws Exception {
        Key key = path.get(0);
        JsonNode output = JsonNodeBase.fromKey(key);

        if (path.size() == 1) {
            output.put(key, value);
        }
        else {
            path.remove(0);
    
            JsonNode child = create(path, value);
            output.put(key, child);
        }

        return output;
    }

    private static class Key {
    
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

    private static interface JsonNode {

        Object toObject();

        void put(Key key, String value) throws JSONException;

        void put(Key key, JsonNode child) throws JSONException;

        boolean has(Key key);

        JsonNode get(Key key) throws JSONException;

    }

    private static interface JsonNodeBase {

        static JsonNode fromObject(Object object) {
            if (object instanceof JSONObject jsonObject)
                return new JsonObjectNode(jsonObject);
            else if (object instanceof JSONArray jsonArray)
                return new JsonArrayNode(jsonArray);
            else
                return null;
        }

        static JsonNode fromKey(Key key) {
            return key.isName ? new JsonObjectNode() : new JsonArrayNode();
        }

    }

    private static class JsonObjectNode implements JsonNode {

        public final JSONObject object;

        JsonObjectNode() {
            this.object = new JSONObject();
        }

        JsonObjectNode(JSONObject object) {
            this.object = object;
        }

        public Object toObject() {
            return object;
        }

        public void put(Key key, String value) throws JSONException {
            object.put(key.name, value);
        }

        public void put(Key key, JsonNode child) throws JSONException {
            object.put(key.name, child.toObject());
        }

        public boolean has(Key key) {
            return object.has(key.name);
        }

        public JsonNode get(Key key) throws JSONException {
            return JsonNodeBase.fromObject(object.get(key.name));
        }

    }

    private static class JsonArrayNode implements JsonNode {

        public final JSONArray array;

        JsonArrayNode() {
            this.array = new JSONArray();
        }

        JsonArrayNode(JSONArray array) {
            this.array = array;
        }

        public Object toObject() {
            return array;
        }

        public void put(Key key, String value) throws JSONException {
            array.put(key.index, value);
        }

        public void put(Key key, JsonNode child) throws JSONException {
            array.put(key.index, child.toObject());
        }

        public boolean has(Key key) {
            return !array.isNull(key.index);
        }

        public JsonNode get(Key key) throws JSONException {
            return JsonNodeBase.fromObject(array.get(key.index));
        }

    }

}

