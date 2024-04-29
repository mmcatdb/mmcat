package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.exception.ExecuteException;

import java.nio.file.Path;
import java.util.Collection;

public interface AbstractControlWrapper {

    boolean isWritable();
    
    boolean isQueryable();

    void execute(Collection<AbstractStatement> statement) throws ExecuteException;

    void execute(Path path) throws ExecuteException;

    AbstractDDLWrapper getDDLWrapper();

    AbstractICWrapper getICWrapper();

    AbstractDMLWrapper getDMLWrapper();

    AbstractPullWrapper getPullWrapper();

    AbstractPathWrapper getPathWrapper();

    AbstractQueryWrapper getQueryWrapper();

}
