package cz.cuni.matfyz.wrapperDummy;

import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.statements.DDLStatement;

import java.util.*;

/**
 *
 * @author jachym.bartik
 */
public class DummyDDLWrapper implements AbstractDDLWrapper
{
    private List<String> methods = new ArrayList<>();
    @Override
    public void setKindName(String name)
    {
        methods.add("setKindName(" + name + ")");
    }

    @Override
    public boolean isSchemaLess()
    {
        methods.add("isSchemaLess()");
        return true;
    }

    @Override
    public boolean addSimpleProperty(Set<String> names, boolean optional)
    {
        methods.add("addSimpleProperty(" + setToString(names) + ", " + optional + ")");
        return true;
    }

    @Override
    public boolean addSimpleArrayProperty(Set<String> names, boolean optional)
    {
        methods.add("addSimpleArrayProperty(" + setToString(names) + ", " + optional + ")");
        return true;
    }

    @Override
    public boolean addComplexProperty(Set<String> names, boolean optional)
    {
        methods.add("addComplexProperty(" + setToString(names) + ", " + optional + ")");
        return true;
    }

    @Override
    public boolean addComplexArrayProperty(Set<String> names, boolean optional)
    {
        methods.add("addComplexArrayProperty(" + setToString(names) + ", " + optional + ")");
        return true;
    }

    @Override
    public DDLStatement createDDLStatement()
    {
        methods.add("createDDLStatement()");

        return new DummyDDLStatement(String.join("\n", methods));
    }

    private String setToString(Set<String> strings)
    {
        var builder = new StringBuilder();

        builder.append("[");
        int index = 0;
        for (String string : strings)
        {
            if (index > 0)
                builder.append(",");
            index++;
            builder.append(" ").append(string);
        }

        if (index > 0)
            builder.append(" ");
        builder.append("]");

        return builder.toString();
    }
}
