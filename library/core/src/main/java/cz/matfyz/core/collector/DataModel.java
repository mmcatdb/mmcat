package cz.matfyz.core.collector;

import java.util.Set;

public class DataModel {

    public static DataModel CreateForQuery(String query, String databaseName, String datasetName) {
        return new DataModel(query, databaseName, datasetName);
    }

    private final QueryData _query;

    public DataModel(String query, String databaseName, String datasetName) {
        _query = new QueryData(query, databaseName, datasetName);
    }

    //ResultData
    public void setResultExecutionTime(double time) { _query.resultData().setExecutionTime(time); }

    public void setResultByteSize(long size) { _query.resultData().resultTable().getSizeInBytes(size); }

    public void setResultSizeInPages(long size) { _query.resultData().resultTable().setSizeInPages(size); }

    public void setResultRowCount(long count) { _query.resultData().resultTable().setRowCount(count); }

    //DatasetData
    public void setDatabaseByteSize(long size) { _query.databaseData().setDatabaseSizeInBytes(size); }
    public void setDatabaseSizeInPages(long size) { _query.databaseData().setDatabaseSizeInPages(size); }
    public void setDatabaseCacheSize(long size) { _query.databaseData().setDatabaseCacheSize(size); }
    public void setPageSize(int size) { _query.databaseData().setDatabasePageSize(size); }
    public int getPageSize() { return _query.databaseData().getDatabasePageSize(); }

    //TableData
    public void setTableByteSize(String tableName, long size) { _query.databaseData().getTable(tableName, true).getSizeInBytes(size); }
    public void setTableSizeInPages(String tableName, long size) { _query.databaseData().getTable(tableName, true).setSizeInPages(size); }
    public void setTableRowCount(String tableName, long count) { _query.databaseData().getTable(tableName, true).setRowCount(count); }
    public void setTableConstraintCount(String tableName, int count) { _query.databaseData().getTable(tableName, true).setConstraintCount(count); }
    public void addTable(String tableName) { _query.databaseData().addTable(tableName); }
    public Set<String> getTableNames() { return _query.databaseData().getTableNames(); }

    //IndexData
    public void setIndexByteSize(String indexName, long size) { _query.databaseData().getIndex(indexName, true).setSizeInBytes(size); }
    public void setIndexSizeInPages(String indexName, long size) { _query.databaseData().getIndex(indexName, true).setSizeInPages(size); }
    public void setIndexRowCount(String indexName, long count) { _query.databaseData().getIndex(indexName, true).setRowCount(count); }
    public void addIndex(String indexName) { _query.databaseData().addIndex(indexName); }
    public Set<String> getIndexNames() { return _query.databaseData().getIndexNames(); }

    //ColumnData
    public void setColumnMandatory(String tableName, String columnName, boolean mandatory) { _query.databaseData().getTable(tableName, true).getColumn(columnName, true).setMandatory(mandatory); }
    public void setColumnDistinctRatio(String tableName, String columnName, double ratio) { _query.databaseData().getTable(tableName, true).getColumn(columnName, true).setDistinctRatio(ratio); }
    public int getColumnMaxByteSize(String tableName, String columnName) { return _query.databaseData().getTable(tableName, false).getColumn(columnName, false).getMaxByteSize(); }

    //ColumnTypeData
    public void setColumnTypeByteSize(String tableName, String columnName, String typeName, int size) { _query.databaseData().getTable(tableName, true).getColumn(columnName, true).getColumnType(typeName, true).setByteSize(size); }
    public void setResultColumnTypeByteSize(String columnName, String typeName, int size) { _query.resultData().resultTable().getColumn(columnName, true).getColumnType(typeName, true).setByteSize(size);}
    public void setColumnTypeRatio(String tableName, String columnName, String typeName, double ratio) { _query.databaseData().getTable(tableName, true).getColumn(columnName, true).getColumnType(typeName, true).setRatio(ratio); }
    public void setResultColumnTypeRatio(String columnName, String typeName, double ratio) { _query.resultData().resultTable().getColumn(columnName, true).getColumnType(typeName, true).setRatio(ratio); }
    public void addColumnType(String tableName, String columnName, String typeName) { _query.databaseData().getTable(tableName, true).getColumn(columnName, true).addType(typeName); }
    public int getColumnTypeByteSize(String tableName, String columnName, String typeName) { return _query.databaseData().getTable(tableName, false).getColumn(columnName, false).getColumnType(typeName, false).getByteSize(); }

    public QueryData toResult() {
        return _query;
    }
}
