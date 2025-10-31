package cz.matfyz.core.collector;

import java.io.Serializable;
import java.util.HashMap;

public class DataModel {

    public static class DatabaseData {
        /** Field containing size of dataset in bytes */
        public Long sizeInBytes;
        /** Field containing size of dataset in pages (virtual disk block size) */
        public Long sizeInPages;
        /** Field containing size of page in bytes */
        public Integer pageSize;
        /** Field containing size of caches in bytes which could be used for query caching */
        public Long cacheSize;

        public final HashMap<String, TableData> tables = new HashMap<>();
        public final HashMap<String, IndexData> indexes = new HashMap<>();

        public TableData getTable(String tableName, boolean createIfNotExist) throws IllegalArgumentException {
            if (!tables.containsKey(tableName) && !createIfNotExist) {
                // throw new IllegalArgumentException("Table '" + tableName + "' does not exists in DataModel");
                return null;
            } else if (!tables.containsKey(tableName)) {
                tables.put(tableName, new TableData());
            }
            return tables.get(tableName);
        }

        public IndexData getIndex(String inxName, boolean createIfNotExist) {
            if (!indexes.containsKey(inxName) && !createIfNotExist) {
                throw new IllegalArgumentException("Index '" + inxName + "' does not exists in DataModel");
            } else if (!indexes.containsKey(inxName)) {
                indexes.put(inxName, new IndexData());
            }
            return indexes.get(inxName);
        }

        public void addTable(String tableName) {
            if (!tables.containsKey(tableName)) tables.put(tableName, new TableData());
        }

        public void addIndex(String inxName) {
            if(!indexes.containsKey(inxName)) indexes.put(inxName, new IndexData());
        }
    }

    public static class ResultData {
        public Double executionTimeMillis = null;
        public final TableData resultTable = new TableData();
    }


    public String databaseID;
    public final DatabaseData database = new DatabaseData();

    public String query; // TODO: change to querycontent, but we don't have the right dependencies (it would have to be in AbstractWrappers), maybe Object or Serializable if we need it would be enough, but id
    public final ResultData result = new ResultData();


    public DataModel(String databaseID, String query) {
        this.databaseID = databaseID;
        this.query = query;
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

        public final HashMap<String, ColumnData> columns;

        private TableData(HashMap<String, ColumnData> columns) {
            this.columns = columns;
        }
        public TableData() {
            this(new HashMap<>());
        }

        public ColumnData getColumn(String columnName, boolean createIfNotExist) throws IllegalArgumentException {
            if (!columns.containsKey(columnName) && createIfNotExist) {
                columns.put(columnName, new ColumnData());
            } else if (!columns.containsKey(columnName) && !createIfNotExist) {
                throw new IllegalArgumentException("Column '" + columnName + "' does not exist");
            }
            return columns.get(columnName);
        }

        @SuppressWarnings({ "unchecked" })
        public TableData clone() {
            final var clone = new TableData((HashMap<String, ColumnData>)columns.clone());
            clone.sizeInBytes = sizeInBytes;
            clone.sizeInPages = sizeInPages;
            clone.rowCount = rowCount;
            clone.constraintCount = constraintCount;
            return clone;
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
