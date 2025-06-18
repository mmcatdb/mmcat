package cz.matfyz.core.collector;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class for saving statistical data about result
 */
public class ResultData {

    /** Field containing execution time in millis */
    @JsonProperty("executionTime")
    private Double _executionTime;

    @JsonProperty("resultTable")
    private final TableData _resultTable;

    public ResultData() {
        _resultTable = new TableData();
        _executionTime = null;
    }

    // Table data setters
    public void setByteSize(long size) {
        _resultTable.setByteSize(size);
    }

    public void setSizeInPages(long sizeInPages) {
        _resultTable.setSizeInPages(sizeInPages);
    }

    public void setRowCount(long count) {
        _resultTable.setRowCount(count);
    }

    public void setExecutionTime(double time) {
        if (_executionTime == null) _executionTime = time;
    }

    @JsonIgnore
    public ColumnData getColumn(String columnName, boolean createIfNotExist) throws IllegalArgumentException { return _resultTable.getColumn(columnName, createIfNotExist); }

    public TableData getResultTable() { return _resultTable; }

    public long getSomeStatistic() { return _resultTable.getSomeStatistic(); }
}
