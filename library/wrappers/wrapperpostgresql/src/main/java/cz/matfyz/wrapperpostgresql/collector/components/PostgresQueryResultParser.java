package cz.matfyz.wrapperpostgresql.collector.components;

import cz.matfyz.abstractwrappers.collector.components.AbstractQueryResultParser;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.core.collector.queryresult.CachedResult;
import cz.matfyz.core.collector.queryresult.ConsumedResult;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class PostgresQueryResultParser extends AbstractQueryResultParser<ResultSet> {

    public PostgresQueryResultParser(WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
    }

    /**
     * Method which adds values to cached result and parse correctly parse them using metaData
     * @param builder builder to accumulate all values
     * @param metaData metaData to get type information about columns
     * @param resultSet native result of parsed query
     * @throws SQLException when sql exception occur
     */
    private void _addDataToBuilder(
            CachedResult.Builder builder,
            ResultSetMetaData metaData,
            ResultSet resultSet
    ) throws SQLException {

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            String className = metaData.getColumnClassName(i);

            Object value;
            if (className.equals("java.lang.Double")) {
                value = resultSet.getDouble(i);
            } else if (className.equals("java.lang.Integer")) {
                value = resultSet.getInt(i);
            } else {
                value = resultSet.getString(i);
            }
            builder.toLastRecordAddValue(columnName, value);
        }
    }

    /**
     * Method which is responsible to consume column types and column names of result
     * @param builder builder to accumulate these information and then build correct result
     * @param metaData metadata object used for getting column info
     * @throws SQLException from accessing metadata
     */
    private void _consumeColumnDataToBuilder(ConsumedResult.Builder builder, ResultSetMetaData metaData) throws SQLException {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String columnName = metaData.getColumnName(i);
            String typeName = metaData.getColumnTypeName(i);

            builder.addColumnType(columnName, typeName);
        }
    }

    /**
     * Method for parsing ordinal result to CachedResult
     * @param resultSet result of some query
     * @return instance of CachedResult
     * @throws ParseException when SQLException occurs during the process
     */
    @Override
    public CachedResult parseResultAndCache(ResultSet resultSet) throws ParseException {
        try {
            var builder = new CachedResult.Builder();
            while (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                builder.addEmptyRecord();
                _addDataToBuilder(builder, metaData, resultSet);
            }
            return builder.toResult();
        } catch (SQLException e) {
            throw getExceptionsFactory().cacheResultFailed(e);
        }
    }

    /**
     * Method which is responsible for executing some query and consume it
     * @param resultSet is native result of some query
     * @return instance of ConsumedResult
     * @throws ParseException when SQLException occur during the process
     */
    @Override
    public ConsumedResult parseResultAndConsume(ResultSet resultSet) throws ParseException {
        try {
            var builder = new ConsumedResult.Builder();
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                builder.addRecord();
                _consumeColumnDataToBuilder(builder, metaData);
            }
            return builder.toResult();
        } catch (SQLException e) {
            throw getExceptionsFactory().consumeResultFailed(e);
        }
    }
}
