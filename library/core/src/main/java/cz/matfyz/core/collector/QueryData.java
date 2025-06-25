package cz.matfyz.core.collector;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Main class holding gathered statistical data which can be eventually transformed to json for persistent storage
 */
public record QueryData(
    /** Field containing dbType for which is this record relevant */
    String systemName,
    /** Field containing name of dataset */
    String databaseName,
    /** Field containing query for which these statistical data were gathered */
    String query, // TODO: change to QueryContent, maybe even relegate the other names to the provider?
    DatabaseData databaseData,
    ResultData resultData
) implements Serializable {

    public QueryData(String systemName, String databaseName, String query) {
        this(systemName, databaseName, query, new DatabaseData(), new ResultData());
    }


    /** Class holding statistical data about dataset */
    public static class DatabaseData implements Serializable {

        /** Field containing size of dataset in bytes */
        private Long _sizeInBytes;
        /** Field containing size of dataset in pages (virtual disk block size) */
        private Long _sizeInPages;
        /** Field containing size of page in bytes */
        private Integer _pageSize;
        /** Field containing size of caches in bytes which could be used for query caching */
        private Long _cacheSize;

        private final HashMap<String, TableData> _tables = new HashMap<>();
        private final HashMap<String, IndexData> _indexes = new HashMap<>();

        public long getDatabaseSizeInBytes() { return _sizeInBytes; }
        public void setDatabaseSizeInBytes(long size) {
            if(_sizeInBytes == null) _sizeInBytes = size;
        }

        public long getDatabaseSizeInPages() { return _sizeInPages; }
        public void setDatabaseSizeInPages(long dataSetSizeInPages) {
            if (_sizeInPages == null) _sizeInPages = dataSetSizeInPages;
        }

        public int getDatabasePageSize() { return _pageSize; }
        public void setDatabasePageSize(int pageSize) {
            if (_pageSize == null) _pageSize = pageSize;
        }

        public long getDatabaseCacheSize() { return _cacheSize; }
        public void setDatabaseCacheSize(long size) {
            if(_cacheSize == null) _cacheSize = size;
        }

        public TableData getTable(String tableName, boolean createIfNotExist) throws IllegalArgumentException {
            if (!_tables.containsKey(tableName) && createIfNotExist) {
                _tables.put(tableName, new TableData());
            } else if (!_tables.containsKey(tableName) && !createIfNotExist) {
                throw new IllegalArgumentException("Table '" + tableName + "' does not exists in DataModel");
            }
            return _tables.get(tableName);
        }

        public IndexData getIndex(String inxName, boolean createIfNotExist) {
            if (!_indexes.containsKey(inxName) && createIfNotExist) {
                _indexes.put(inxName, new IndexData());
            } else if (!_indexes.containsKey(inxName) && !createIfNotExist) {
                throw new IllegalArgumentException("Index '" + inxName + "' does not exists in DataModel");
            }
            return _indexes.get(inxName);
        }

        public Set<String> getTableNames() { return _tables.keySet(); }
        public void addTable(String tableName) {
            if (!_tables.containsKey(tableName)) _tables.put(tableName, new TableData());
        }

        public Set<String> getIndexNames() { return _indexes.keySet(); }
        public void addIndex(String inxName) {
            if(!_indexes.containsKey(inxName)) _indexes.put(inxName, new IndexData());
        }
    }


    /** Class for saving statistical data about result */
    public static class ResultData implements Serializable {
        /** Field containing execution time in milliseconds */
        private Double _executionTime = null;

        private final TableData _resultTable = new TableData();

        public TableData resultTable() { return _resultTable; }

        /** Get execution time in milliseconds. */
        public Double getExecutionTime() { return _executionTime; }

        public void setExecutionTime(double millis) {
            if (_executionTime == null) _executionTime = millis;
        }
    }


    /** Class holding statistical data about table */
    public static class TableData implements Serializable {

        /** Field containing size of table in bytes */
        private Long _sizeInBytes;
        /** Field containing size of table in pages */
        private Long _sizeInPages;
        /** Field containing row count of table */
        private Long _rowCount;
        /** Field containing number of constraints defined over table */
        private Long _constraintCount;

        private final HashMap<String, ColumnData> _columns = new HashMap<>();

        public long getSizeInBytes() { return _sizeInBytes; }
        public void getSizeInBytes(long sizeInBytes) {
            if (_sizeInBytes == null) _sizeInBytes = sizeInBytes;
        }

        public long getSizeInPages() { return _sizeInPages; }
        public void setSizeInPages(long sizeInPages) {
            if(_sizeInPages == null) _sizeInPages = sizeInPages;
        }

        public long getRowCount() { return _rowCount; }
        public void setRowCount(long count) {
            if (_rowCount == null) _rowCount = count;
        }

        public long getConstraintCount() { return _constraintCount; }
        public void setConstraintCount(long count) {
            if (_constraintCount == null) _constraintCount = count;
        }

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
        private Double _valuesRatio;
        /** Field holding dominant data type of column. */
        private final HashMap<String, ColumnType> _types;
        /** Field holding information if column is mandatory to be set for entity in database. */
        private Boolean _mandatory;

        public ColumnData() {
            _valuesRatio = null;
            _mandatory = null;
            _types = new HashMap<>();
        }

        public int getMaxByteSize() {
            return _types.values().stream().map(ColumnType::getByteSize).max(Integer::compareTo).orElse(0);
        }

        public void addType(String columnType) {
            if (!_types.containsKey(columnType)) _types.put(columnType, new ColumnType());
        }

        public ColumnType getColumnType(String columnType, boolean createNew) {
            if (!_types.containsKey(columnType) && createNew) {
                _types.put(columnType, new ColumnType());
            } else if (!_types.containsKey(columnType) && !createNew) {
                throw new IllegalArgumentException("Column '" + columnType + "' does not exists in DataModel");
            }
            return _types.get(columnType);
        }

        public boolean getMandatory() { return _mandatory; }
        public void setMandatory(boolean value) {
            if (_mandatory == null)
                _mandatory = value;
        }

        public double getDistinctRatio() { return _valuesRatio; }
        public void setDistinctRatio(double ratio) {
            if (_valuesRatio == null) {
                _valuesRatio = ratio;}
        }
    }


    public static class ColumnType implements Serializable {

        private Integer _byteSize;
        private Double _ratio;

        public int getByteSize() { return _byteSize; }
        public void setByteSize(int size) {
            if (_byteSize == null)
                _byteSize = size;
        }

        public double getRatio() { return _ratio; }
        public void setRatio(double ratio) {
            if (_ratio == null)
                _ratio = ratio;
        }
    }


    /** Class for saving statistical data about index */
    public static class IndexData implements Serializable {
        /** Field holding index size in bytes */
        public Long _sizeInBytes;
        /** Field holding index size in pages */
        public Long _sizeInPages;
        /** Field holding index row count */
        public Long _rowCount;

        public IndexData() {
            _sizeInBytes = null;
            _sizeInPages = null;
            _rowCount = null;
        }

        public long getSizeInBytes() { return _sizeInBytes; }
        public void setSizeInBytes(long size) {
            if (_sizeInBytes == null) {_sizeInBytes = size;}
        }

        public long getSizeInPages() { return _sizeInPages; }
        public void setSizeInPages(long sizeInPages) {
            if (_sizeInPages == null) {_sizeInPages = sizeInPages;}
        }

        public long getRowCount() { return _rowCount; }
        public void setRowCount(long count) {
            if (_rowCount == null) {_rowCount = count;}
        }
    }

}
