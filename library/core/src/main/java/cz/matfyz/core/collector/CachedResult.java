package cz.matfyz.core.collector;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class representing Cached result with unified API from different databases
 */
public class CachedResult {

    /** List of Maps, which contains all data fetched from native results */
    private final List<Map<String, Object>> records;

    /** Private pointer, which points to actual record when iterating over result */
    private int cursor;

    private CachedResult(List<Map<String, Object>> records) {
        this.records = records;
        cursor = -1;
    }

    /**
     * Method for iterating over result
     * @return true if there exists next result
     */
    public boolean next() {
        cursor++;
        return cursor < records.size();
    }

    /**
     * Method for repointing cursor to beginning so the result can be iterated again
     */
    public void refresh() { cursor = -1; }

    /**
     * Method for checking of this collection of records have column of this columnName
     * @param colName inputted columnName
     * @return true if this column exists
     */
    public boolean containsCol(String colName) {
        return records.get(cursor).containsKey(colName);
    }

    /**
     * Private method used for getting value of column from actual record which is pointed by cursor
     * @param colName inputted columnName
     * @return instance of Object which is selected value or null this value do not exist
     */
    private Object get(String colName) {
        return records.get(cursor).getOrDefault(colName, null);
    }

    /**
     * Method which gets value from selected column as object and then tries to parse it or convert it to Integer and return it
     * @param colName inputted columnName
     * @return int value
     * @throws ClassCastException when gathered value cannot be parsed or is null
     */
    public int getInt(String colName) {
        Object value = get(colName);
        if (value == null) {
            throw new ClassCastException("Cannot cast null to int");
        }
        else if (value instanceof Integer intValue) {
            return intValue;
        }
        else if (value instanceof Long longValue) {
            if (longValue < Integer.MAX_VALUE && longValue > Integer.MIN_VALUE) {
                return longValue.intValue();
            }
            else {
                throw new ClassCastException("Cannot cast long to int, because it is out of range");
            }
        }
        else if (value instanceof String strValue){
            return Integer.parseInt(strValue);
        }
        else {
            throw new ClassCastException();
        }
    }

    /**
     * Method which tries to return value from selected column as String
     * @param colName inputted columnName
     * @return converted string value
     */
    public String getString(String colName) {
        return (String)get(colName);
    }

    /**
     * Method which tries to get value as double from selected column
     * @param colName inputted columnName to select column
     * @return converted double value
     * @throws ClassCastException when value do not exist or can't be converted
     */
    public double getDouble(String colName) {
        Object value = get(colName);
        if (value == null) {
            throw new ClassCastException("Cannot cast null to double");
        }
        else if (value instanceof Double doubleValue) {
            return doubleValue;
        }
        else if (value instanceof String strValue){
            return Double.parseDouble(strValue);
        }
        else {
            throw new ClassCastException();
        }
    }

    /**
     * Method which tries to get value from selected column as boolean
     * @param colName inputted columnName to select column
     * @return converted boolean value
     * @throws ClassCastException when value can't be parsed to boolean or value do not exist
     */
    public boolean getBoolean(String colName) {
        Object value = get(colName);
        if (value == null) {
            throw new ClassCastException("Cannot cast null to boolean");
        } else if (value instanceof Boolean booleanValue) {
            return booleanValue;
        } else if (value instanceof String strValue) {
            return Boolean.parseBoolean(strValue);
        } else {
            throw new ClassCastException();
        }
    }

    /**
     * Method which tries to get value from selected column as long
     * @param colName inputted columnName
     * @return converted value
     * @throws ClassCastException when value can't be parsed to long or value do not exist
     */
    public long getLong(String colName) {
        Object value = get(colName);
        if (value == null) {
            throw  new ClassCastException("Cannot cast null to long");
        } else if (value instanceof Long longValue) {
            return longValue;
        } else if (value instanceof Integer intValue) {
            return intValue;
        } else if (value instanceof Double doubleValue) {
            return Math.round(doubleValue);
        } else if (value instanceof String strValue) {
            return new BigDecimal(strValue).longValue();
        } else {
            throw new ClassCastException();
        }
    }

    /**
     * Private method used to iteratively parse object map to string, object one, which can be used for building org.bson.Document
     * @param objectMap inputted object map
     * @return parsed string, object map
     */
    private Map<String, Object> parseToStringMap(Map<?, ?> objectMap) {
        Map<String, Object> stringMap = new HashMap<>();
        for (Entry<?, ?> entry : objectMap.entrySet()) {
            if (entry.getKey() instanceof String strValue) {
                stringMap.put(strValue, entry.getValue());
            }
        }
        return stringMap;
    }

    /**
     * Mathod which tries to get value from selected column as a Map
     * @param colName inputted column
     * @return value as a Map
     */
    public Map<String, Object> getMap(String colName) {
        Object value = get(colName);
        if (value == null) {
            throw new ClassCastException("Cannot cast null to Document");
        } else if (value instanceof Map<?, ?> mapValue) {
            return parseToStringMap(mapValue);
        } else {
            throw new ClassCastException();
        }
    }

    /**
     * Private generic method for converting list of objects to list of specified types
     * @param listValue input list
     * @param clazz input class to which method should try to convert each object in list
     * @return converted list
     * @param <T> class type
     */
    private <T> List<T> convertList(List<?> listValue, Class<T> clazz) {
        List<T> convertedList = new ArrayList<>();
        for (Object item : listValue) {
            convertedList.add(clazz.cast(item));
        }
        return convertedList;
    }

    /**
     * Method which get value as list of specified type by generic parameter
     * @param columnName to select column
     * @param clazz to select type
     * @return list of specified types
     * @param <T> generic parameter for type
     * @throws ClassCastException when list cannot be converted
     */
    public <T> List<T> getList(String columnName, Class<T> clazz) {
        Object value = get(columnName);
        if (value instanceof List<?> listValue) {
            return convertList(listValue, clazz);
        }
        throw new ClassCastException();
    }

    /**
     * Method for getting number of records inside result
     * @return record count
     */
    public int getRowCount() {
        return records.size();
    }

    /**
     * Builder class which represents builder responsible for building CachedResult and filling it with all records
     */
    public static class Builder {
        private final List<Map<String, Object>> records;


        public Builder() {
            records = new ArrayList<>();
        }

        /**
         * Method which will add new empty record
         */
        public void addEmptyRecord() {
            records.add(new LinkedHashMap<>());
        }

        /**
         * Method which will add new value into specified column to last record
         * @param colName to specify column by columnName
         * @param value inputted value
         */
        public void toLastRecordAddValue(String colName, Object value) {
            int lastInx = records.size() - 1;
            records.get(lastInx).put(colName, value);
        }

        /**
         * Method for building Build instance to CachedResult
         * @return built result
         */
        public CachedResult toResult() {
            return new CachedResult(records);
        }
    }
}
