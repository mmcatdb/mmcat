package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;
import cz.matfyz.core.record.AdminerFilter;

import org.json.JSONObject;

import java.util.List;

public interface AbstractPullWrapper {

    ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException;

    QueryResult executeQuery(QueryStatement statement);

    JSONObject getTableNames(String limit, String offset);

    JSONObject getTable(String tableName, String limit, String offset);

    JSONObject getRows(String tableName, List<AdminerFilter> filter, String limit, String offset);

}
