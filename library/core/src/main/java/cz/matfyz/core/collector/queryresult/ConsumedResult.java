package cz.matfyz.core.collector.queryresult;

import java.util.*;

/**
 * Class which represents ConsumedResult and provides it unified API to get measured statistical data
 */
public class ConsumedResult {

    /** Field containing all columnTypes for all columns of result */
    private final Map<String, Map<String, Integer>> _columnTypes;

    /** Field holding byte size of result measured in bytes */
    private final long _byteSize;

    /** Field containing record count from result */
    private final long _count;

    private ConsumedResult(Map<String, Map<String, Integer>> columnTypes, long byteSize, long count) {
        _byteSize = byteSize;
        _count = count;
        _columnTypes = columnTypes;
    }

    /**
     * Method for getting all column names present in result
     * @return set of the column names
     */
    public Set<String> getColumnNames() {
        return _columnTypes.keySet();
    }

    /**
     * Method for getting type for specific column
     * @param colName to select column by columnName
     * @return type of this column as string
     */
    public Iterable<String> getColumnTypes(String colName) {
        return _columnTypes.get(colName).keySet();
    }

    public String getMajorType(String colName) {
        var counts = _columnTypes.get(colName);
        return Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public String getOnlyType(String colName) {
        String[] types = _columnTypes.get(colName).keySet().toArray(new String[]{});
        if (types.length == 1)
            return types[0];
        throw new UnsupportedOperationException("There must be only type for column name: " + colName);
    }

    public double getColumnTypeRatio(String colName, String type) {
        return (double) _columnTypes.get(colName).get(type) / (double)_count;
    }

    /**
     * Getter for field _byteSize
     * @return byte size of result
     */
    public long getByteSize() {
        return _byteSize;
    }

    /**
     * Getter for field _rowCount
     * @return row count of result
     */
    public long getRowCount() {
        return _count;
    }

    /**
     * Class representing Builder for ConsumedResult
     */
    public static class Builder {

        private final Map<String, Map<String, Integer>> _columnTypes;
        private long _byteSize;
        private long _count;

        public Builder() {
            _columnTypes = new HashMap<>();
            _count = 0;
            _byteSize = 0;
        }

        private void addColumnType(Map<String, Integer> counts, String type) {
            counts.compute(type, (k, v) -> v == null ? 1 : v + 1);
        }

        /**
         * Method for adding type for specific column
         * @param colName to specify column by columnName
         * @param type inputted type
         */
        public void addColumnType(String colName, String type) {
            if (_columnTypes.containsKey(colName)) {
                var counts = _columnTypes.get(colName);
                addColumnType(counts, type);
            } else {
                var counts = new HashMap<String, Integer>();
                addColumnType(counts, type);
                _columnTypes.put(colName, counts);
            }
        }

        /**
         * Method for incrementing count of records
         */
        public void addRecord() {
            _count += 1;
        }

        /**
         * Method for adding more byteSize to result. It can therefore be added incrementally while cycling over native result
         * @param size added size
         */
        public void addByteSize(int size) {
            _byteSize += size;
        }

        /**
         * Builder method for building instance of ConsumedResult from Builder
         * @return newly created ConsumedResult
         */
        public ConsumedResult toResult() {
            return new ConsumedResult(
                    _columnTypes,
                    _byteSize,
                    _count
            );
        }
    }

}
