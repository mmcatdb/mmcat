package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.abstractwrappers.exception.ExecuteException;

import java.util.Collection;

/**
 * @author pavel.koupil
 */
public interface AbstractControlWrapper {

    public abstract void execute(Collection<AbstractStatement> statement) throws ExecuteException;

    public abstract AbstractDDLWrapper getDDLWrapper();

    public abstract AbstractICWrapper getICWrapper();

    public abstract AbstractDMLWrapper getDMLWrapper();

    public abstract AbstractPullWrapper getPullWrapper();

    public abstract AbstractPathWrapper getPathWrapper();

}
