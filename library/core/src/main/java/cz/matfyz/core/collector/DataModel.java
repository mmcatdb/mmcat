package cz.matfyz.core.collector;

import java.util.Set;

public interface DataModel {
    public static DataModel CreateForQuery(String query, String databaseName, String datasetName) {
        return new QueryDataModel(query, databaseName, datasetName);
    }

    public void setResultExecutionTime(double time);
    public void setResultByteSize(long size);
    public void setResultSizeInPages(long size);
    public void setResultRowCount(long count);

    public void setDatabaseByteSize(long size);
    public void setDatabaseSizeInPages(long size);
    public void setDatabaseCacheSize(long size);
    public void setPageSize(int size);
    public int getPageSize();

    public void setTableByteSize(String tableName, long size);
    public void setTableSizeInPages(String tableName, long size);
    public void setTableRowCount(String tableName, long count);
    public void setTableConstraintCount(String tableName, int count);
    public void addTable(String tableName);
    public Set<String> getTableNames();

    public void setIndexByteSize(String indexName, long size);
    public void setIndexSizeInPages(String indexName, long size);
    public void setIndexRowCount(String indexName, long count);
    public void addIndex(String indexName);
    public Set<String> getIndexNames();

    public void setColumnMandatory(String tableName, String columnName, boolean mandatory);
    public void setColumnDistinctRatio(String tableName, String columnName, double ratio);
    public int getColumnMaxByteSize(String tableName, String columnName);

    public void setColumnTypeByteSize(String tableName, String columnName, String typeName, int size);
    public void setResultColumnTypeByteSize(String columnName, String typeName, int size);
    public void setColumnTypeRatio(String tableName, String columnName, String typeName, double ratio);
    public void setResultColumnTypeRatio(String columnName, String typeName, double ratio);
    public void addColumnType(String tableName, String columnName, String typeName);

    public int getColumnTypeByteSize(String tableName, String columnName, String typeName);

    public String toJson() throws DataModelException;

    public long getSomeStatistic();
}
