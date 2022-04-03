package cz.cuni.matfyz.wrapperDummy;

import cz.cuni.matfyz.abstractWrappers.AbstractPushWrapper;
import cz.cuni.matfyz.statements.DMLStatement;

import java.util.*;

/**
 *
 * @author jachym.bartik
 */
public class DummyPushWrapper implements AbstractPushWrapper
{
    //private List<String> methods = new ArrayList<>();

    private final List<DMLTestStructure> structures = new ArrayList<>();
    private DMLTestStructure structure;

    public List<DMLTestStructure> structures()
    {
        return structures;
    }
    
    @Override
    public void setKindName(String name)
    {
        //methods.add("setKindName(" + name + ")");
        structure = new DMLTestStructure(name);
    }

    @Override
    public void append(String name, Object value)
    {
        //methods.add("append(" + name + ", " + value + ")");
        structure.add("append(" + name + ", " + value + ")");
    }

    @Override
    public void clear()
    {
        //methods.add("clear()");
        structure = null;
    }

    @Override
    public DMLStatement createDMLStatement()
    {
        //methods.add("createDMLStatement()");
        structures.add(structure);
        return new DummyDMLStatement("");
    }
}
