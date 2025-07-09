package cz.matfyz.wrapperpostgresql.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLExceptionsFactory;
import cz.matfyz.wrapperpostgresql.collector.PostgreSQLResources;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.collector.DataModel.TableData;

import java.util.HashSet;
import java.util.Set;

/**
 * Class which is responsible for collecting all statistical data and save them to data model
 */
public class PostgresDataCollector {
    private final PostgresConnection connection;
    private final PostgresQueryResultParser resultParser;

    protected final String databaseName;
    protected final DataModel model;


    public PostgresDataCollector(
            String databaseName,
            DataModel dataModel,
            PostgresConnection connection,
            PostgresQueryResultParser resultParser
    ) throws ConnectionException {
        this.databaseName = databaseName;
        this.model = dataModel;
        this.connection = connection;
        this.resultParser = resultParser;
    }

    protected CachedResult executeQuery(String query) throws DataCollectException {
        try {
            return resultParser.parseResultAndCache(connection.executeQuery(query));
        } catch (QueryExecutionException | ParseException e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    //saving of database data

    /**
     * Method which saves page size to model
     * @throws DataCollectException when help query fails
     */
    private void collectPageSize() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getPageSizeQuery());
        if (result.next()) {
            int pageSize = result.getInt("current_setting");
            model.databasePageSize = pageSize;
        }
    }

    /**
     * Method which counts and saves dataset size in pages to model
     * @param size byte size of dataset
     */
    private void collectDatabaseSizeInPages(long size) {
        int pageSize = model.databasePageSize;
        if (pageSize > 0) {
            long sizeInPages = (long) Math.ceil((double)size / (double)pageSize);
            model.databaseSizeInPages = sizeInPages;
        }
    }

    /**
     * Method which saves sizes of dataset to model
     * @throws DataCollectException when help query fails
     */
    private void collectDatabaseDataSizes() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getDatasetSizeQuery(databaseName));
        if (result.next()) {
            long dataSetSize = result.getLong("pg_database_size");
            model.databaseSizeInBytes = dataSetSize;
            collectDatabaseSizeInPages(dataSetSize);
        }
    }

    /**
     * Method which saves size of caches used by postgres and save it to model
     * @throws DataCollectException when help query fails
     */
    private void collectDatabaseCacheSize() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getCacheSizeQuery());
        if (result.next()) {
            long size = result.getLong("shared_buffers");
            model.databaseCacheSize = size;
        }
    }

    /**
     * Method to save all dataset data to model
     * @throws DataCollectException when some of the help queries failed
     */
    private void collectDatabaseData() throws DataCollectException {
        collectPageSize();
        collectDatabaseDataSizes();
        collectDatabaseCacheSize();
    }

    //Saving of columns data

    /**
     * Method which saves data for specific column
     * @param tableName identify table
     * @param colName select column
     * @throws DataCollectException when help query fails
     */
    private void collectNumericDataForCol(String tableName, String colName, String typeName) throws DataCollectException {
        CachedResult res = executeQuery(PostgreSQLResources.getColDataQuery(tableName, colName));
        if (res.next()) {
            double ratio = res.getDouble("n_distinct");
            int size = res.getInt("avg_width");
            model.getTable(tableName, true).getColumn(colName, true).distinctRatio = ratio;
            model.getTable(tableName, true).getColumn(colName, true).getColumnType(typeName, true).byteSize = size;
        }

    }

    /**
     * Method which saves type and if column is mandatory (nullable)
     * @param tableName to specify table
     * @param colName to select column
     * @throws DataCollectException when help query fails
     */
    private void collectTypeAndMandatoryForCol(String tableName, String colName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getColTypeAndMandatoryQuery(tableName, colName));
        if (result.next()) {
            String type = result.getString("typname");
            collectNumericDataForCol(tableName, colName, type);

            boolean mandatory = result.getBoolean("attnotnull");
            model.getTable(tableName, true).getColumn(colName, true).mandatory = mandatory;
        }
    }

    /**
     * Method which gets all column names for specific table
     * @param tableName to specify table
     * @return set of column names
     * @throws DataCollectException when help query fails
     */
    private Set<String> getColumnNames(String tableName) throws DataCollectException {
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
    private void collectColumnData(String tableName, TableData table) throws DataCollectException {
        for (String columnName: getColumnNames(tableName)) {
            collectTypeAndMandatoryForCol(tableName, columnName);
        }
    }

    // Saving of tables data

    /**
     * Method which saves table row count to model
     * @param tableName to specify table
     * @throws DataCollectException when help query fails
     */
    private void collectTableRowCount(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getRowCountForTableQuery(tableName));
        if (result.next()) {
            table.rowCount = result.getLong("reltuples");
        }
    }

    /**
     * Method which saves count of table constraints to model
     * @param tableName to specify table
     * @throws DataCollectException when help query fails
     */
    private void collectTableConstraintCount(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getConstraintsCountForTableQuery(tableName));
        if (result.next()) {
            table.constraintCount = result.getLong("relchecks");
        }
    }

    /**
     * Method which saves table size in pages ot model
     *
     * @param tableName identify table
     * @throws DataCollectException when help query fails
     */
    private void collectTableSizeInPages(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeInPagesQuery(tableName));
        if (result.next()) {
            table.sizeInPages = result.getLong("relpages");
        }
    }

    /**
     * Method which saves table size to model
     *
     * @param tableName specifies table
     * @throws DataCollectException when help query fails
     */
    private void collectTableSize(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeQuery(tableName));
        if (result.next()) {
            long size = result.getLong("pg_total_relation_size");
            table.sizeInBytes = size;
        }
    }

    /**
     * Method for saving all table data
     * @throws DataCollectException when some of the help queries fails
     */
    private void collectTableData() throws DataCollectException {
        for (String tableName : model.databaseTables.keySet()) {
            final var table = model.getTable(tableName, true);
            collectTableRowCount(tableName, table);
            collectTableConstraintCount(tableName, table);
            collectTableSizeInPages(tableName, table);
            collectTableSize(tableName, table);
            collectColumnData(tableName, table);
        }
    }

    //saving of index data

    /**
     * Method which saves table name for over which was built used index
     *
     * @param indexName identify index
     * @throws DataCollectException when help query fails
     */
    private void collectIndexTableName(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableNameForIndexQuery(indexName));
        if (result.next()) {
            String tableName = result.getString("tablename");
            model.addTable(tableName);
        }
    }

    /**
     * Method which saves index row count to model
     *
     * @param indexName index identifier
     * @throws DataCollectException when help query fails
     */
    private void collectIndexRowCount(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getRowCountForTableQuery(indexName));
        if (result.next()) {
            long rowCount = result.getLong("reltuples");
            model.getIndex(indexName, true).rowCount = rowCount;
        }
    }

    /**
     * Method for saving index size in pages to data model
     *
     * @param indexName to specify index
     * @throws DataCollectException when help query fails
     */
    private void collectIndexSizeInPages(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeInPagesQuery(indexName));
        if (result.next()) {
            long sizeInPages = result.getLong("relpages");
            model.getIndex(indexName, true).sizeInPages = sizeInPages;
        }
    }

    /**
     * Method for saving index size to data model
     *
     * @param indexName to specify index
     * @throws DataCollectException when help query fails
     */
    private void collectIndexSize(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeQuery(indexName));
        if (result.next()) {
            long size = result.getLong("pg_total_relation_size");
            model.getIndex(indexName, true).sizeInBytes = size;
        }
    }

    /**
     * Method for saving all index data
     * @throws DataCollectException when some of the help queries fails
     */
    private void collectIndexData() throws DataCollectException {
        for (String indexName: model.databaseIndexes.keySet()) {
            collectIndexTableName(indexName);
            collectIndexRowCount(indexName);
            collectIndexSizeInPages(indexName);
            collectIndexSize(indexName);
        }
    }

    /**
     * Method which saves statistics about the main result
     * @param mainResult main result for which we want to save stats
     * @throws DataCollectException when no table for some column was found
     */
    private void collectResultData(ConsumedResult mainResult) throws DataCollectException {
        long rowCount = mainResult.getRowCount();
        model.resultTable.rowCount = rowCount;

        long sizeInBytes = 0;
        double colSize = 0;
        for (String tableColumn : mainResult.getColumnNames()) {
            colSize = 0;
            for (String colType : mainResult.getColumnTypes(tableColumn)) {
                final var splitTableColumn = tableColumn.split("\".\"");
                String tableName = splitTableColumn[0].substring(1);
                String columnName = splitTableColumn[1].substring(0, splitTableColumn[1].length() - 1);

                int typeSize = model.getTable(tableName, false).getColumn(columnName, false).getColumnType(colType, false).byteSize;

                // FIXME: this assigns a wrong column name to the resultTable (i.e. the original table name, but we probably need the one after projection)
                model.resultTable.getColumn(columnName, true).getColumnType(colType, true).byteSize = typeSize;
                double ratio = mainResult.getColumnTypeRatio(tableColumn, colType);
                model.resultTable.getColumn(columnName, true).getColumnType(colType, true).ratio = ratio;
                colSize += typeSize * ratio;
            }
            sizeInBytes += Math.round(colSize);
        }
        sizeInBytes *= rowCount;
        model.resultTable.sizeInBytes = (sizeInBytes);

        int pageSize = model.databasePageSize;
        if (pageSize > 0)
            model.resultTable.sizeInPages = (long)Math.ceil((double) sizeInBytes / pageSize);
    }

    /**
     * Public method which collects all statistical data after main query execution
     * @param result result of main query for which will wrapper collects all the data
     * @throws DataCollectException when some help queries failed
     */
    public void collectData(ConsumedResult result) throws DataCollectException {
        collectDatabaseData();
        collectIndexData();
        collectTableData();
        collectResultData(result);
    }

}
