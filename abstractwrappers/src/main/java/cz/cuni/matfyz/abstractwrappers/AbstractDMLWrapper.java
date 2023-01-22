package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.statements.AbstractStatement;

/**
 * @author pavel.koupil
 */
public interface AbstractDMLWrapper {

    public abstract void setKindName(String name);

    public abstract void append(String name, Object value);

    public abstract AbstractStatement createDMLStatement();

    public abstract void clear();

}
