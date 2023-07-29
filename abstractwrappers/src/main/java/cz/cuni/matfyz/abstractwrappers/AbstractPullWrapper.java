package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.abstractwrappers.exception.PullForestException;
import cz.cuni.matfyz.abstractwrappers.utils.PullQuery;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.record.ForestOfRecords;

/**
 * @author pavel.koupil, jachym.bartik
 */
public interface AbstractPullWrapper {

    ForestOfRecords pullForest(ComplexProperty path, PullQuery query) throws PullForestException;

}
