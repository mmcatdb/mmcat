package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.abstractwrappers.exception.ExecuteException;

import java.util.Collection;

/**
 * @author pavel.koupil
 */
public interface AbstractControlWrapper {

    void execute(Collection<AbstractStatement> statement) throws ExecuteException;

    AbstractDDLWrapper getDDLWrapper();

    AbstractICWrapper getICWrapper();

    AbstractDMLWrapper getDMLWrapper();

    AbstractPullWrapper getPullWrapper();

    AbstractPathWrapper getPathWrapper();

    AbstractQueryWrapper getQueryWrapper();

}
