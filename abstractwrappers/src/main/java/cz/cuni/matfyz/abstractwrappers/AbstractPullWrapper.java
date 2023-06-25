package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.abstractwrappers.exception.PullForestException;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.record.ForestOfRecords;

/**
 * @author pavel.koupil, jachym.bartik
 */
public interface AbstractPullWrapper {

    public abstract ForestOfRecords pullForest(ComplexProperty path, PullWrapperOptions options) throws PullForestException;

}
