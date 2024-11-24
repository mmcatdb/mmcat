package cz.matfyz.wrapperdummy;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class DMLTestStructure {

    private String name;

    private final List<String> values = new ArrayList<>();

    public DMLTestStructure(String name) {
        this.name = name;
    }

    public void add(String value) {
        values.add(value);
    }

    public DMLTestStructure(JSONObject object) throws JSONException {
        name = object.getString("name");
        var array = object.getJSONArray("values");
        for (int i = 0; i < array.length(); i++)
            add(array.getString(i));
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(name).append(": [\n");
        for (final String value : values.stream().sorted().toList())
            builder.append("    ").append(value).append(",\n");

        builder.append("]");

        return builder.toString();
    }
}
