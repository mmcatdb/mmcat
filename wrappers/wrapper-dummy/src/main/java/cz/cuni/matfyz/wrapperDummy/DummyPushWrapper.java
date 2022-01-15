package cz.cuni.matfyz.wrapperDummy;

import cz.cuni.matfyz.abstractwrappers.AbstractPushWrapper;
import cz.cuni.matfyz.statements.DMLStatement;

import java.util.*;

/**
 *
 * @author jachym.bartik
 */
public class DummyPushWrapper implements AbstractPushWrapper
{
    private List<String> methods = new ArrayList<>();

    public Iterable<String> methods()
    {
        return methods;
    }
    
    @Override
    public void setKindName(String name)
    {
        methods.add("setKindName(" + name + ")");
    }

    @Override
    public void append(String name, Object value)
    {
        methods.add("append(" + name + ", " + value + ")");
    }

    @Override
    public void clear()
    {
        methods.add("clear()");
    }

    @Override
    public DMLStatement createDMLStatement()
    {
        methods.add("createDMLStatement()");
        return new DummyDMLStatement("");
    }
}
