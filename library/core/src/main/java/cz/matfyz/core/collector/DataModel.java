package cz.matfyz.core.collector;

import java.io.Serializable;
import java.util.HashMap;

public class DataModel {

    public String databaseID; // TODO: how to identify an underlying system within mmcat? simply dbType, e.g. MongoDB, or some other way?
    public String query; // TODO: change to querycontent, but we don't have the right dependencies (it would have to be in AbstractWrappers), maybe Object or Serializable if we need it would be enough, but id

    /** Field containing size of dataset in bytes */
    public Long databaseSizeInBytes;
    /** Field containing size of dataset in pages (virtual disk block size) */
    public Long databaseSizeInPages;
    /** Field containing size of page in bytes */
    public Integer databasePageSize;
    /** Field containing size of caches in bytes which could be used for query caching */
    public Long databaseCacheSize;

    public final HashMap<String, TableData> databaseTables = new HashMap<>();
    public final HashMap<String, IndexData> databaseIndexes = new HashMap<>();

    public Double executionTimeMillis = null;
    public final TableData resultTable = new TableData();

    public DataModel(String databaseID, String query) {
        this.databaseID = databaseID;
        this.query = query;
    }



    public TableData getTable(String tableName, boolean createIfNotExist) throws IllegalArgumentException {
        if (!databaseTables.containsKey(tableName) && !createIfNotExist) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exists in DataModel");
        } else if (!databaseTables.containsKey(tableName)) {
            databaseTables.put(tableName, new TableData());
        }
        return databaseTables.get(tableName);
    }

    public IndexData getIndex(String inxName, boolean createIfNotExist) {
        if (!databaseIndexes.containsKey(inxName) && !createIfNotExist) {
            throw new IllegalArgumentException("Index '" + inxName + "' does not exists in DataModel");
        } else if (!databaseIndexes.containsKey(inxName)) {
            databaseIndexes.put(inxName, new IndexData());
        }
        return databaseIndexes.get(inxName);
    }

    public void addTable(String tableName) {
        if (!databaseTables.containsKey(tableName)) databaseTables.put(tableName, new TableData());
    }

    public void addIndex(String inxName) {
        if(!databaseIndexes.containsKey(inxName)) databaseIndexes.put(inxName, new IndexData());
    }

    /** Class holding statistical data about table */
    public static class TableData implements Serializable {

        /** Field containing size of table in bytes */
        public Long sizeInBytes;
        /** Field containing size of table in pages */
        public Long sizeInPages;
        /** Field containing row count of table */
        public Long rowCount;
        /** Field containing number of constraints defined over table */
        public Long constraintCount;

        public final HashMap<String, ColumnData> _columns = new HashMap<>();

        public ColumnData getColumn(String columnName, boolean createIfNotExist) throws IllegalArgumentException {
            if (!_columns.containsKey(columnName) && createIfNotExist) {
                _columns.put(columnName, new ColumnData());
            } else if (!_columns.containsKey(columnName) && !createIfNotExist) {
                throw new IllegalArgumentException("Column '" + columnName + "' does not exist");
            }
            return _columns.get(columnName);
        }
    }


    /** Class responsible for representing collected statistical data about individual columns */
    public static class ColumnData implements Serializable {

        /** Field holding information about statistical distribution of values. In PostgreSQL it holds ratio of distinct values. */
        public Double distinctRatio;
        /** Field holding information if column is mandatory to be set for entity in database. */
        public Boolean mandatory;
        /** Field holding dominant data type of column. */
        public final HashMap<String, ColumnType> types = new HashMap<>();

        public int getMaxByteSize() {
            return types.values().stream().map(ct -> ct.byteSize).max(Integer::compareTo).orElse(0);
        }

        public void addType(String columnType) {
            if (!types.containsKey(columnType)) types.put(columnType, new ColumnType());
        }

        public ColumnType getColumnType(String columnType, boolean createNew) {
            if (!types.containsKey(columnType) && createNew) {
                types.put(columnType, new ColumnType());
            } else if (!types.containsKey(columnType) && !createNew) {
                throw new IllegalArgumentException("Column '" + columnType + "' does not exists in DataModel");
            }
            return types.get(columnType);
        }
    }

    public static class ColumnType {
        public Integer byteSize;
        public Double ratio;
    }

    public static class IndexData implements Serializable {
        public Long sizeInBytes;
        public Long sizeInPages;
        public Long rowCount;
    }

    // TODO: maybe later make a method to convert the data into an immutable (perhaps structured) record
}
