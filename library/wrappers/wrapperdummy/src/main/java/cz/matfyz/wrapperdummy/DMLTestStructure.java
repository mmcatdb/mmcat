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
        final var sb = new StringBuilder();

        sb.append(name).append(": [\n");
        for (final String value : values.stream().sorted().toList())
            sb.append("    ").append(value).append(",\n");

        sb.append("]");

        return sb.toString();
    }
}
