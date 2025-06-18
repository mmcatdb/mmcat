package cz.matfyz.core.collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/** Class holding statistical data about dataset */
public class DatabaseData {

    /** Field containing size of dataset in bytes */
    @JsonProperty("databaseSize")
    private Long _databaseSize;
    /** Field containing size of dataset in pages (virtual disk block size) */
    @JsonProperty("databaseSizeInPages")
    private Long _databaseSizeInPages;
    /** Field containing size of page in bytes */
    @JsonProperty("pageSize")
    private Integer _pageSize;
    /** Field containing size of caches in bytes which could be used for query caching */
    @JsonProperty("cacheSize")
    private Long _cacheSize;

    @JsonProperty("tables")
    private final HashMap<String, TableData> _tables;
    @JsonProperty("indexes")
    private final HashMap<String, IndexData> _indexes;

    public DatabaseData() {
        _tables = new HashMap<>();
        _indexes = new HashMap<>();

        _databaseSize = null;
        _databaseSizeInPages = null;
        _pageSize = null;
        _cacheSize = null;
    }

    // Database setting methods
    public void setDatabaseSize(long size) {
        if(_databaseSize == null)
            _databaseSize = size;
    }

    public void setDatabaseSizeInPages(long dataSetSizeInPages) {
        if (_databaseSizeInPages == null) { _databaseSizeInPages = dataSetSizeInPages; }
    }
    public void setDatabasePageSize(int pageSize) {
        if (_pageSize == null)
            _pageSize = pageSize;
    }

    @JsonIgnore
    public int getDatabasePageSize() {
        return _pageSize;
    }

    public void setDatabaseCacheSize(long size) {
        if(_cacheSize == null)
            _cacheSize = size;
    }

    @JsonIgnore
    public TableData getTable(String tableName, boolean createIfNotExist) throws IllegalArgumentException {
        if (!_tables.containsKey(tableName) && createIfNotExist) {
            _tables.put(tableName, new TableData());
        } else if (!_tables.containsKey(tableName) && !createIfNotExist) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exists in DataModel");
        }
        return _tables.get(tableName);
    }

    @JsonIgnore
    public IndexData getIndex(String inxName, boolean createIfNotExist) {
        if (!_indexes.containsKey(inxName) && createIfNotExist) {
            _indexes.put(inxName, new IndexData());
        } else if (!_indexes.containsKey(inxName) && !createIfNotExist) {
            throw new IllegalArgumentException("Index '" + inxName + "' does not exists in DataModel");
        }
        return _indexes.get(inxName);
    }

    @JsonIgnore
    public Set<String> getTableNames() {
        return _tables.keySet();
    }
    public void addTable(String tableName) {
        if (!_tables.containsKey(tableName)) {
            _tables.put(tableName, new TableData());
        }
    }

    @JsonIgnore
    public Set<String> getIndexNames() {
        return _indexes.keySet();
    }
    public void addIndex(String inxName) {
        if(!_indexes.containsKey(inxName)) {
            _indexes.put(inxName, new IndexData());
        }
    }
}
