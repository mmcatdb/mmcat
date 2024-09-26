package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.AbstractQueryWrapper.QueryStatement;
import cz.matfyz.abstractwrappers.exception.PullForestException;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.querying.queryresult.QueryResult;
import cz.matfyz.core.record.ForestOfRecords;
import org.json.JSONArray;

public interface AbstractPullWrapper {

    ForestOfRecords pullForest(ComplexProperty path, QueryContent query) throws PullForestException;

    QueryResult executeQuery(QueryStatement statement);

    JSONArray getTableNames(String limit, String offset);

    JSONArray getTable(String tableName, String limit, String offset);

    JSONArray getRows(String tableName, String columnName, String columnValue, String operator, String limit, String offset);

}
