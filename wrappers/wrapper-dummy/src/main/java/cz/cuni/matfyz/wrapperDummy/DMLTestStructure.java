package cz.cuni.matfyz.wrapperDummy;

import java.util.*;
import org.json.JSONObject;

/**
 *
 * @author jachym.bartik
 */
public class DMLTestStructure
{
    private String name;

    private final List<String> values = new ArrayList<>();

    public DMLTestStructure(String name)
    {
        this.name = name;
    }

    public void add(String value)
    {
        values.add(value);
    }

    public DMLTestStructure(JSONObject object) throws Exception
    {
        name = object.getString("name");
        var array = object.getJSONArray("values");
        for (int i = 0; i < array.length(); i++)
            add(array.getString(i));
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof DMLTestStructure structure)
        {
            if (!name.equals(structure.name))
                return false;

            if (values.size() != structure.values.size())
                return false;

            var set = new TreeSet<String>(values);
            var otherSet = new TreeSet<String>(structure.values);

            return set.equals(otherSet);
        }

        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append(name).append(": {\n");
        for (String value : values)
            builder.append("    ").append(value).append("\n");

        builder.append("}\n");

        return builder.toString();
    }
}
