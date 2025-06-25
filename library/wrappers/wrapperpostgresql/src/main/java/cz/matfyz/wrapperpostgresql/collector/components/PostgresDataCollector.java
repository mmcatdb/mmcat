package cz.matfyz.wrapperpostgresql.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLExceptionsFactory;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLResources;
import cz.matfyz.core.collector.queryresult.ConsumedResult;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.collector.queryresult.CachedResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Class which is responsible for collecting all statistical data and save them to data model
 */
public class PostgresDataCollector {
    private final PostgresConnection _connection;
    private final PostgresQueryResultParser _resultParser;

    protected final String _databaseName;
    protected final DataModel _model;


    public PostgresDataCollector(
            String databaseName,
            DataModel dataModel,
            PostgresConnection connection,
            PostgresQueryResultParser resultParser
    ) throws ConnectionException {
        _databaseName = databaseName;
        _model = dataModel;
        _connection = connection;
        _resultParser = resultParser;
    }

    protected CachedResult executeQuery(String query) throws DataCollectException {
        try {
            return _resultParser.parseResultAndCache(_connection.executeQuery(query));
        } catch (QueryExecutionException | ParseException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    protected ConsumedResult executeQueryAndConsume(String query) throws DataCollectException {
        try {
            return _resultParser.parseResultAndConsume(_connection.executeQuery(query));
        } catch (QueryExecutionException | ParseException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    //saving of database data

    /**
     * Method which saves page size to model
     * @throws DataCollectException when help query fails
     */
    private void _collectPageSize() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getPageSizeQuery());
        if (result.next()) {
            int pageSize = result.getInt("current_setting");
            _model.setPageSize(pageSize);
        }
    }

    /**
     * Method which counts and saves dataset size in pages to model
     * @param size byte size of dataset
     */
    private void _collectDatabaseSizeInPages(long size) {
        int pageSize = _model.getPageSize();
        if (pageSize > 0) {
            long sizeInPages = (long) Math.ceil((double)size / (double)pageSize);
            _model.setDatabaseSizeInPages(sizeInPages);
        }
    }

    /**
     * Method which saves sizes of dataset to model
     * @throws DataCollectException when help query fails
     */
    private void _collectDatabaseDataSizes() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getDatasetSizeQuery(_databaseName));
        if (result.next()) {
            long dataSetSize = result.getLong("pg_database_size");
            _model.setDatabaseByteSize(dataSetSize);
            _collectDatabaseSizeInPages(dataSetSize);
        }
    }

    /**
     * Method which saves size of caches used by postgres and save it to model
     * @throws DataCollectException when help query fails
     */
    private void _collectDatabaseCacheSize() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getCacheSizeQuery());
        if (result.next()) {
            long size = result.getLong("shared_buffers");
            _model.setDatabaseCacheSize(size);
        }
    }

    /**
     * Method to save all dataset data to model
     * @throws DataCollectException when some of the help queries failed
     */
    private void _collectDatabaseData() throws DataCollectException {
        _collectPageSize();
        _collectDatabaseDataSizes();
        _collectDatabaseCacheSize();
    }

    //Saving of columns data

    /**
     * Method which saves data for specific column
     * @param tableName identify table
     * @param colName select column
     * @throws DataCollectException when help query fails
     */
    private void _collectNumericDataForCol(String tableName, String colName, String typeName) throws DataCollectException {
        CachedResult res = executeQuery(PostgreSQLResources.getColDataQuery(tableName, colName));
        if (res.next()) {
            double ratio = res.getDouble("n_distinct");
            int size = res.getInt("avg_width");
            _model.setColumnDistinctRatio(tableName, colName, ratio);
            _model.setColumnTypeByteSize(tableName, colName, typeName, size);
        }

    }

    /**
     * Method which saves type and if column is mandatory (nullable)
     * @param tableName to specify table
     * @param colName to select column
     * @throws DataCollectException when help query fails
     */
    private void _collectTypeAndMandatoryForCol(String tableName, String colName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getColTypeAndMandatoryQuery(tableName, colName));
        if (result.next()) {
            String type = result.getString("typname");
            _collectNumericDataForCol(tableName, colName, type);

            boolean mandatory = result.getBoolean("attnotnull");
            _model.setColumnMandatory(tableName, colName, mandatory);
        }
    }

    /**
     * Method which gets all column names for specific table
     * @param tableName to specify table
     * @return set of column names
     * @throws DataCollectException when help query fails
     */
    private Set<String> _getColumnNames(String tableName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getColNamesForTableQuery(tableName));
        Set<String> names = new HashSet<>();

        while (result.next()) {
            String name = result.getString("attname");
            names.add(name);
        }
        return names;
    }

    /**
     * Method which saves all column data for some table
     * @param tableName to specify table
     * @throws DataCollectException when some of the help queries fails
     */
    private void _collectColumnData(String tableName) throws DataCollectException {
        for (String columnName: _getColumnNames(tableName)) {
            _collectTypeAndMandatoryForCol(tableName, columnName);
        }
    }

    // Saving of tables data

    /**
     * Method which saves table row count to model
     * @param tableName to specify table
     * @throws DataCollectException when help query fails
     */
    private void _collectTableRowCount(String tableName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getRowCountForTableQuery(tableName));
        if (result.next()) {
            long rowCount = result.getLong("reltuples");
            _model.setTableRowCount(tableName, rowCount);
        }
    }

    /**
     * Method which saves count of table constraints to model
     * @param tableName to specify table
     * @throws DataCollectException when help query fails
     */
    private void _collectTableConstraintCount(String tableName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getConstraintsCountForTableQuery(tableName));
        if (result.next()) {
            int count = result.getInt("relchecks");
            _model.setTableConstraintCount(tableName, count);
        }
    }

    /**
     * Method which saves table size in pages ot model
     *
     * @param tableName identify table
     * @throws DataCollectException when help query fails
     */
    private void _collectTableSizeInPages(String tableName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeInPagesQuery(tableName));
        if (result.next()) {
            long sizeInPages = result.getLong("relpages");
            _model.setTableSizeInPages(tableName, sizeInPages);
        }
    }

    /**
     * Method which saves table size to model
     *
     * @param tableName specifies table
     * @throws DataCollectException when help query fails
     */
    private void _collectTableSize(String tableName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeQuery(tableName));
        if (result.next()) {
            long size = result.getLong("pg_total_relation_size");
            _model.setTableByteSize(tableName, size);
        }
    }

    /**
     * Method for saving all table data
     * @throws DataCollectException when some of the help queries fails
     */
    private void _collectTableData() throws DataCollectException {
        for (String tableName : _model.getTableNames()) {
            _collectTableRowCount(tableName);
            _collectTableConstraintCount(tableName);
            _collectTableSizeInPages(tableName);
            _collectTableSize(tableName);
            _collectColumnData(tableName);
        }
    }

    //saving of index data

    /**
     * Method which saves table name for over which was built used index
     *
     * @param indexName identify index
     * @throws DataCollectException when help query fails
     */
    private void _collectIndexTableName(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableNameForIndexQuery(indexName));
        if (result.next()) {
            String tableName = result.getString("tablename");
            _model.addTable(tableName);
        }
    }

    /**
     * Method which saves index row count to model
     *
     * @param indexName index identifier
     * @throws DataCollectException when help query fails
     */
    private void _collectIndexRowCount(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getRowCountForTableQuery(indexName));
        if (result.next()) {
            long rowCount = result.getLong("reltuples");
            _model.setIndexRowCount(indexName, rowCount);
        }
    }

    /**
     * Method for saving index size in pages to data model
     *
     * @param indexName to specify index
     * @throws DataCollectException when help query fails
     */
    private void _collectIndexSizeInPages(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeInPagesQuery(indexName));
        if (result.next()) {
            long sizeInPages = result.getLong("relpages");
            _model.setIndexSizeInPages(indexName, sizeInPages);
        }
    }

    /**
     * Method for saving index size to data model
     *
     * @param indexName to specify index
     * @throws DataCollectException when help query fails
     */
    private void _collectIndexSize(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeQuery(indexName));
        if (result.next()) {
            long size = result.getLong("pg_total_relation_size");
            _model.setIndexByteSize(indexName, size);
        }
    }

    /**
     * Method for saving all index data
     * @throws DataCollectException when some of the help queries fails
     */
    private void _collectIndexData() throws DataCollectException {
        for (String indexName: _model.getIndexNames()) {
            _collectIndexTableName(indexName);
            _collectIndexRowCount(indexName);
            _collectIndexSizeInPages(indexName);
            _collectIndexSize(indexName);
        }
    }


    /**
     * Method which gets table name for column based on its name and type
     * @param columnName specified column name
     * @param columnType specified type
     * @return corresponding table name
     * @throws DataCollectException when no table for some column was found
     */
    private String _getTableNameForColumn(String columnName, String columnType) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableNameForColumnQuery(columnName, columnType));
        while (result.next()) {
            String tableName = result.getString("relname");
            if (_model.getTableNames().contains(tableName)) {
                return tableName;
            }
        }
        throw PostgreSQLExceptionsFactory.getExceptionsFactory().tableForColumnNotFound(columnName);
    }

    /**
     * Method which saves statistics about the main result
     * @param mainResult main result for which we want to save stats
     * @throws DataCollectException when no table for some column was found
     */
    private void _collectResultData(ConsumedResult mainResult) throws DataCollectException {
        long rowCount = mainResult.getRowCount();
        _model.setResultRowCount(rowCount);

        long sizeInBytes = 0;
        double colSize = 0;
        for (String columnName : mainResult.getColumnNames()) {
            colSize = 0;
            for (String colType : mainResult.getColumnTypes(columnName)) {
                String tableName = _getTableNameForColumn(columnName, colType);
                int typeSize = _model.getColumnTypeByteSize(tableName, columnName, colType);
                _model.setResultColumnTypeByteSize(columnName, colType, typeSize);
                double ratio = mainResult.getColumnTypeRatio(columnName, colType);
                _model.setResultColumnTypeRatio(columnName, colType, ratio);
                colSize += typeSize * ratio;
            }
            sizeInBytes += Math.round(colSize);
        }
        sizeInBytes *= rowCount;
        _model.setResultByteSize(sizeInBytes);

        int pageSize = _model.getPageSize();
        if (pageSize > 0)
            _model.setResultSizeInPages((int)Math.ceil((double) sizeInBytes / pageSize));
    }

    /**
     * Public method which collects all statistical data after main query execution
     * @param result result of main query for which will wrapper collects all the data
     * @throws DataCollectException when some help queries failed
     */
    public void collectData(ConsumedResult result) throws DataCollectException {
        _collectDatabaseData();
        _collectIndexData();
        _collectTableData();
        _collectResultData(result);
    }

}
