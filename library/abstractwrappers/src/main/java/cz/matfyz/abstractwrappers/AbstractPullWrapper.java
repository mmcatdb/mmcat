package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.queryresult.QueryResult;
import cz.matfyz.abstractwrappers.utils.PullQuery;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.record.ForestOfRecords;

/**
 * @author pavel.koupil, jachym.bartik
 */
public interface AbstractPullWrapper {

    ForestOfRecords pullForest(ComplexProperty path, PullQuery query) throws PullForestException;

    QueryResult executeQuery(QueryStatement statement);

}
