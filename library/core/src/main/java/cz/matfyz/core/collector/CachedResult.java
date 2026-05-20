package cz.matfyz.core.collector;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * Class representing Cached result with unified API from different databases
 */
public class CachedResult {

    /** Contains all data fetched from native results. */
    private final List<Map<String, Object>> records;

    /** Points to actual record when iterating over result. */
    private int cursor;

    private CachedResult(List<Map<String, Object>> records) {
        this.records = records;
        cursor = -1;
    }

    /** @return true if there exists next result */
    public boolean next() {
        cursor++;
        return cursor < records.size();
    }

    /** Repoints cursor to beginning so the result can be iterated again. */
    public void refresh() {
        cursor = -1;
    }

    public boolean containsCol(String colName) {
        return records.get(cursor).containsKey(colName);
    }

    private Object get(String colName) {
        return records.get(cursor).getOrDefault(colName, null);
    }

    /** @throws ClassCastException */
    public int getInt(String colName) {
        Object value = get(colName);

        if (value == null)
            throw new ClassCastException("Cannot cast null to int");

        if (value instanceof Integer intValue)
            return intValue;

        if (value instanceof Long longValue) {
            if (longValue >= Integer.MAX_VALUE && longValue <= Integer.MIN_VALUE)
                throw new ClassCastException("Cannot cast long to int, because it is out of range");

            return longValue.intValue();
        }

        if (value instanceof String strValue)
            return Integer.parseInt(strValue);

        throw new ClassCastException();
    }

    public String getString(String colName) {
        return (String)get(colName);
    }

    /** @throws ClassCastException */
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

    /** @throws ClassCastException */
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

    /** @throws ClassCastException */
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

    private Map<String, Object> parseToStringMap(Map<?, ?> objectMap) {
        Map<String, Object> stringMap = new HashMap<>();
        for (Entry<?, ?> entry : objectMap.entrySet()) {
            if (entry.getKey() instanceof String strValue) {
                stringMap.put(strValue, entry.getValue());
            }
        }
        return stringMap;
    }

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

    private <T> List<T> convertList(List<?> listValue, Class<T> clazz) {
        List<T> convertedList = new ArrayList<>();
        for (Object item : listValue) {
            convertedList.add(clazz.cast(item));
        }
        return convertedList;
    }

    /** @throws ClassCastException */
    public <T> List<T> getList(String columnName, Class<T> clazz) {
        Object value = get(columnName);
        if (value instanceof List<?> listValue) {
            return convertList(listValue, clazz);
        }
        throw new ClassCastException();
    }

    public int getRowCount() {
        return records.size();
    }

    public static class Builder {

        private final List<Map<String, Object>> records;

        public Builder() {
            records = new ArrayList<>();
        }

        public void addEmptyRecord() {
            records.add(new LinkedHashMap<>());
        }

        public void toLastRecordAddValue(String colName, Object value) {
            int lastInx = records.size() - 1;
            records.get(lastInx).put(colName, value);
        }

        public CachedResult build() {
            return new CachedResult(records);
        }

    }

}
