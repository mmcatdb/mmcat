package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.adminer.ForeignKey;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.AdminerFilter;

import org.json.JSONObject;

import java.util.List;

public interface AbstractPullWrapper {

    ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException;

    QueryResult executeQuery(QueryStatement statement);

    JSONObject getKindNames(String limit, String offset);

    JSONObject getKind(String kindName, String limit, String offset);

    JSONObject getRows(String kindName, List<AdminerFilter> filter, String limit, String offset);

    /**
     * Retrieves a list of foreign key relationships for the specified kind.
     *
     * @param kindName The name of the kind for which to fetch foreign key relationships.
     * @return A {@link List} of {@link ForeignKey} objects representing the foreign key relationships.
     */
    List<ForeignKey> getForeignKeys(String kindName);

}
