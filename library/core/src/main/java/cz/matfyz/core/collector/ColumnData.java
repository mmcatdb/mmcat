package cz.matfyz.core.collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * Class responsible for representing collected statistical data about individual columns
 */
public class ColumnData {

    /**
     * Field holding information about statistical distribution of values. In PostgreSQL it holds ratio of distinct values.
     */
    @JsonProperty("ratio")
    private Double _valuesRatio;

    /**
     * Field holding dominant data type of column.
     */
    @JsonProperty("types")
    private final HashMap<String, ColumnType> _types;

    /**
     * Field holding information if column is mandatory to be set for entity in database.
     */
    @JsonProperty("mandatory")
    private Boolean _mandatory;

    public ColumnData() {
        _valuesRatio = null;
        _mandatory = null;
        _types = new HashMap<>();
    }

    /**
     * Getter for Field _size
     *
     * @return value stored in _size field
     */
    @JsonIgnore
    public int getMaxByteSize() {
        return _types.values().stream().map(ColumnType::getByteSize).max(Integer::compareTo).orElse(0);
    }

    /**
     * Add new column type
     * @param columnType to be added
     */
    public void addType(String columnType) {
        if (!_types.containsKey(columnType))
            _types.put(columnType, new ColumnType());
    }

    @JsonIgnore
    public ColumnType getColumnType(String columnType, boolean createNew) {
        if (!_types.containsKey(columnType) && createNew) {
            _types.put(columnType, new ColumnType());
        } else if (!_types.containsKey(columnType) && !createNew) {
            throw new IllegalArgumentException("Column '" + columnType + "' does not exists in DataModel");
        }
        return _types.get(columnType);
    }

    public void setMandatory(boolean value) {
        if (_mandatory == null)
            _mandatory = value;
    }

    public void setDistinctRatio(double ratio) {
        if (_valuesRatio == null) {
            _valuesRatio = ratio;}
    }
}
