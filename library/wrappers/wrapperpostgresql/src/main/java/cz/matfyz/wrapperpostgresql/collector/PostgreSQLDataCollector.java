package cz.matfyz.wrapperpostgresql.collector;

import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.collector.DataModel.TableData;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Class which is responsible for collecting all statistical data and save them to data model
 */
public class PostgreSQLDataCollector {

    private final DataModel model;
    private final PostgreSQLProvider provider;
    private final PostgreSQLQueryResultParser resultParser;
    private final String databaseName;

    public PostgreSQLDataCollector(DataModel model, PostgreSQLProvider provider, PostgreSQLQueryResultParser resultParser, String databaseName) {
        this.model = model;
        this.provider = provider;
        this.resultParser = resultParser;
        this.databaseName = databaseName;
    }

    private CachedResult executeQuery(String query) throws DataCollectException {
        try (
            var connection = provider.getConnection();
            var statement = connection.createStatement();
        ) {
            final var result = statement.executeQuery(query);
            return resultParser.parseResultAndCache(result);
        } catch (Exception e) {
            throw PostgreSQLExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    /**
     * Collects all statistical data after main query execution.
     * @param result result of main query for which will wrapper collects all the data
     */
    public void collectData(ConsumedResult result) throws DataCollectException {
        collectDatabaseData();
        collectTableData();
        collectIndexData();
        collectResultData(result);
    }

    // #region Database

    private void collectDatabaseData() throws DataCollectException {
        collectPageSize();
        collectDatabaseDataSizes();
        collectDatabaseCacheSize();
    }

    private void collectPageSize() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getPageSizeQuery());
        if (result.next()) {
            int pageSize = result.getInt("current_setting");
            model.database.pageSize = pageSize;
        }
    }

    private void collectDatabaseDataSizes() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getDatasetSizeQuery(databaseName));
        if (result.next()) {
            long dataSetSize = result.getLong("pg_database_size");
            model.database.sizeInBytes = dataSetSize;
            collectDatabaseSizeInPages(dataSetSize);
        }
    }

    private void collectDatabaseSizeInPages(long size) {
        int pageSize = model.database.pageSize;
        if (pageSize > 0) {
            long sizeInPages = (long) Math.ceil((double) size / (double) pageSize);
            model.database.sizeInPages = sizeInPages;
        }
    }

    private void collectDatabaseCacheSize() throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getCacheSizeQuery());
        if (result.next()) {
            long size = result.getLong("shared_buffers");
            model.database.cacheSize = size;
        }
    }

    // #endregion
    // #region Table

    private void collectTableData() throws DataCollectException {
        for (String tableName : model.database.tables.keySet()) {
            final var table = model.database.getTable(tableName, true);
            collectTableRowCount(tableName, table);
            collectTableConstraintCount(tableName, table);
            collectTableSizeInPages(tableName, table);
            collectTableSize(tableName, table);
            collectColumnData(tableName, table);
        }
    }

    private void collectTableRowCount(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getRowCountForTableQuery(tableName));
        if (result.next()) {
            table.rowCount = result.getLong("reltuples");
        }
    }

    private void collectTableConstraintCount(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getConstraintsCountForTableQuery(tableName));
        if (result.next()) {
            table.constraintCount = result.getLong("relchecks");
        }
    }

    private void collectTableSizeInPages(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeInPagesQuery(tableName));
        if (result.next()) {
            table.sizeInPages = result.getLong("relpages");
        }
    }

    private void collectTableSize(String tableName, TableData table) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeQuery(tableName));
        if (result.next()) {
            long size = result.getLong("pg_total_relation_size");
            table.sizeInBytes = size;
        }
    }

    // #endregion
    // #region Column

    private void collectColumnData(String tableName, TableData table) throws DataCollectException {
        for (String columnName: getColumnNames(tableName))
            collectTypeAndMandatoryForCol(tableName, columnName);
    }

    private Set<String> getColumnNames(String tableName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getColNamesForTableQuery(tableName));
        Set<String> names = new HashSet<>();

        while (result.next()) {
            String name = result.getString("attname");
            names.add(name);
        }
        return names;
    }

    private void collectTypeAndMandatoryForCol(String tableName, String colName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getColTypeAndMandatoryQuery(tableName, colName));
        if (result.next()) {
            String type = result.getString("typname");
            collectNumericDataForCol(tableName, colName, type);

            boolean mandatory = result.getBoolean("attnotnull");
            model.database.getTable(tableName, true).getColumn(colName, true).mandatory = mandatory;
        }
    }

    private void collectNumericDataForCol(String tableName, String colName, String typeName) throws DataCollectException {
        CachedResult res = executeQuery(PostgreSQLResources.getColDataQuery(tableName, colName));
        if (res.next()) {
            double ratio = res.getDouble("n_distinct");
            int size = res.getInt("avg_width");
            model.database.getTable(tableName, true).getColumn(colName, true).distinctRatio = ratio;
            model.database.getTable(tableName, true).getColumn(colName, true).getColumnType(typeName, true).byteSize = size;
        }

    }

    // #endregion
    // #region Index

    private void collectIndexData() throws DataCollectException {
        for (String indexName: model.database.indexes.keySet()) {
            collectIndexTableName(indexName);
            collectIndexRowCount(indexName);
            collectIndexSizeInPages(indexName);
            collectIndexSize(indexName);
        }
    }

    private void collectIndexTableName(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableNameForIndexQuery(indexName));
        if (result.next()) {
            String tableName = result.getString("tablename");
            model.database.addTable(tableName);
        }
    }

    private void collectIndexRowCount(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getRowCountForTableQuery(indexName));
        if (result.next()) {
            long rowCount = result.getLong("reltuples");
            model.database.getIndex(indexName, true).rowCount = rowCount;
        }
    }

    private void collectIndexSizeInPages(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeInPagesQuery(indexName));
        if (result.next()) {
            long sizeInPages = result.getLong("relpages");
            model.database.getIndex(indexName, true).sizeInPages = sizeInPages;
        }
    }

    private void collectIndexSize(String indexName) throws DataCollectException {
        CachedResult result = executeQuery(PostgreSQLResources.getTableSizeQuery(indexName));
        if (result.next()) {
            long size = result.getLong("pg_total_relation_size");
            model.database.getIndex(indexName, true).sizeInBytes = size;
        }
    }

    // #endregion
    // #region Result

    private void collectResultData(ConsumedResult mainResult) throws DataCollectException {
        long rowCount = mainResult.getRowCount();
        model.result.resultTable.rowCount = rowCount;

        long sizeInBytes = 0;
        double colSize = 0;
        for (String tableColumn : mainResult.getColumnNames()) {
            colSize = 0;
            for (String colType : mainResult.getColumnTypes(tableColumn)) {
                final var splitTableColumn = tableColumn.split("\".\"");
                String tableName = splitTableColumn[0].substring(1);
                String columnName = splitTableColumn[1].substring(0, splitTableColumn[1].length() - 1);

                int typeSize = model.database.getTable(tableName, false).getColumn(columnName, false).getColumnType(colType, false).byteSize;

                // FIXME: this assigns a wrong column name to the resultTable (i.e. the original table name, but we probably need the one after projection)
                model.result.resultTable.getColumn(columnName, true).getColumnType(colType, true).byteSize = typeSize;
                double ratio = mainResult.getColumnTypeRatio(tableColumn, colType);
                model.result.resultTable.getColumn(columnName, true).getColumnType(colType, true).ratio = ratio;
                colSize += typeSize * ratio;
            }
            sizeInBytes += Math.round(colSize);
        }
        sizeInBytes *= rowCount;
        model.result.resultTable.sizeInBytes = (sizeInBytes);

        int pageSize = model.database.pageSize;
        if (pageSize > 0)
            model.result.resultTable.sizeInPages = (long)Math.ceil((double) sizeInBytes / pageSize);
    }

    // #endregion

}
