package cz.matfyz.core.collector;

import java.util.*;

/**
 * Class which represents ConsumedResult and provides it unified API to get measured statistical data
 */
public class ConsumedResult {

    /** Field containing all columnTypes for all columns of result */
    private final Map<String, Map<String, Integer>> columnTypes;

    /** Field holding byte size of result measured in bytes */
    private final long byteSize;

    /** Field containing record count from result */
    private final long count;

    private ConsumedResult(Map<String, Map<String, Integer>> columnTypes, long byteSize, long count) {
        this.byteSize = byteSize;
        this.count = count;
        this.columnTypes = columnTypes;
    }

    /**
     * Method for getting all column names present in result
     * @return set of the column names
     */
    public Set<String> getColumnNames() {
        return columnTypes.keySet();
    }

    /**
     * Method for getting type for specific column
     * @param colName to select column by columnName
     * @return type of this column as string
     */
    public Iterable<String> getColumnTypes(String colName) {
        return columnTypes.get(colName).keySet();
    }

    public String getMajorType(String colName) {
        var counts = columnTypes.get(colName);
        return Collections.max(counts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public String getOnlyType(String colName) {
        String[] types = columnTypes.get(colName).keySet().toArray(new String[]{});
        if (types.length == 1)
            return types[0];
        throw new UnsupportedOperationException("There must be only type for column name: " + colName);
    }

    public double getColumnTypeRatio(String colName, String type) {
        return (double) columnTypes.get(colName).get(type) / (double)count;
    }

    /**
     * Getter for field byteSize
     * @return byte size of result
     */
    public long getByteSize() {
        return byteSize;
    }

    /**
     * Getter for field rowCount
     * @return row count of result
     */
    public long getRowCount() {
        return count;
    }

    /**
     * Class representing Builder for ConsumedResult
     */
    public static class Builder {

        private final Map<String, Map<String, Integer>> columnTypes;
        private long byteSize;
        private long count;

        public Builder() {
            columnTypes = new HashMap<>();
            count = 0;
            byteSize = 0;
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
            if (columnTypes.containsKey(colName)) {
                var counts = columnTypes.get(colName);
                addColumnType(counts, type);
            } else {
                var counts = new HashMap<String, Integer>();
                addColumnType(counts, type);
                columnTypes.put(colName, counts);
            }
        }

        /**
         * Method for incrementing count of records
         */
        public void addRecord() {
            count += 1;
        }

        /**
         * Method for adding more byteSize to result. It can therefore be added incrementally while cycling over native result
         * @param size added size
         */
        public void addByteSize(int size) {
            byteSize += size;
        }

        /**
         * Builder method for building instance of ConsumedResult from Builder
         * @return newly created ConsumedResult
         */
        public ConsumedResult toResult() {
            return new ConsumedResult(
                    columnTypes,
                    byteSize,
                    count
            );
        }
    }

}
