package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class DummyDMLWrapper implements AbstractDMLWrapper {
    //private List<String> methods = new ArrayList<>();

    @Override public void clear() {
        //methods.add("clear()");
        structure = null;
    }

    private DMLTestStructure structure;
    private final List<DMLTestStructure> structures = new ArrayList<>();

    public List<DMLTestStructure> structures() {
        return structures;
    }

    @Override public void setKindName(String name) {
        //methods.add("setKindName(" + name + ")");
        structure = new DMLTestStructure(name);
    }

    @Override public void append(String name, @Nullable Object value) {
        //methods.add("append(" + name + ", " + value + ")");
        structure.add("append(" + name + ", " + value + ")");
    }

    @Override public AbstractStatement createDMLStatement() {
        //methods.add("createDMLStatement()");
        structures.add(structure);
        return AbstractStatement.createEmpty();
    }
}
