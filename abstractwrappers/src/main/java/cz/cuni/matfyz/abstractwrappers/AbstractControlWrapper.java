package cz.cuni.matfyz.abstractwrappers;

import java.util.Collection;

/**
 * @author pavel.koupil
 */
public interface AbstractControlWrapper {

    public abstract boolean execute(Collection<AbstractStatement> statement);

    public abstract AbstractDDLWrapper getDDLWrapper();

    public abstract AbstractICWrapper getICWrapper();

    public abstract AbstractDMLWrapper getDMLWrapper();

    public abstract AbstractPullWrapper getPullWrapper();

    public abstract AbstractPathWrapper getPathWrapper();

}
