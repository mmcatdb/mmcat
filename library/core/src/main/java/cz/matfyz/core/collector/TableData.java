package cz.matfyz.core.collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

/**
 * Class holding statistical data about table
 */
public class TableData {

    /** Field containing size of table in bytes */
    @JsonProperty("byteSize")
    private Long _size;

    /** Field containing size of table in pages */
    @JsonProperty("sizeInPages")
    private Long _sizeInPages;

    /** Field containing row count of table */
    @JsonProperty("rowCount")
    private Long _rowCount;

    /** Field containing number of constraints defined over table */
    @JsonProperty("constraintCount")
    private Long _constraintCount;

    @JsonProperty("columns")
    private final HashMap<String, ColumnData> _columns;

    public TableData() {
        _columns = new HashMap<>();
        _size = null;
        _sizeInPages = null;
        _rowCount = null;
        _constraintCount = null;
    }

    //Tables setting methods
    public void setByteSize(long size) {
        if (_size == null)
            _size = size;
    }

    public void setSizeInPages(long sizeInPages) {
        if(_sizeInPages == null)
            _sizeInPages = sizeInPages;
    }

    public void setRowCount(long count) {
        if (_rowCount == null)
            _rowCount = count;
    }

    public void setConstraintCount(long count) {
        if (_constraintCount == null) {
            _constraintCount = count;
        }
    }

    @JsonIgnore
    public ColumnData getColumn(String columnName, boolean createIfNotExist) throws IllegalArgumentException {
        if (!_columns.containsKey(columnName) && createIfNotExist) {
            _columns.put(columnName, new ColumnData());
        } else if (!_columns.containsKey(columnName) && !createIfNotExist) {
            throw new IllegalArgumentException("Column '" + columnName + "' does not exist");
        }
        return _columns.get(columnName);
    }

    public long getSomeStatistic() { return _size; }
}
