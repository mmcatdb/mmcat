package de.hda.fbi.modules.schemaextraction.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public enum DataType {
    STRING(String.class), DOUBLE(Double.class), FLOAT(Float.class), INTEGER(Integer.class), LONG(Long.class), DATE(
            Date.class), BOOLEAN(Boolean.class);

    private final Class<?> _type;

    DataType(Class<?> type) {
        _type = type;
    }

    public Class<?> getClassFromEnum() {
        return _type;
    }

    public Object parse(String value, DataType type) throws IllegalArgumentException {
        switch (type) {
            case DOUBLE:
                return Double.valueOf(value);
            case STRING:
                return value;
            case BOOLEAN:
                return Boolean.valueOf(value);
            case DATE:
                try {
                    return DateFormat.getInstance().parse(value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(value + " is not a date.", e);
                }
            case FLOAT:
                return Float.valueOf(value);
            case INTEGER:
                return Integer.valueOf(value);
            case LONG:
                return Long.valueOf(value);
            default:
                return value;
        }
    }
}
