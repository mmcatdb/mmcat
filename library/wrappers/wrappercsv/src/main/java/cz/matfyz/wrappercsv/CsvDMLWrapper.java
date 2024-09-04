package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.exception.OtherException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A Data Manipulation Language (DML) wrapper for CSV that implements the {@link AbstractDMLWrapper} interface.
 * This class provides methods to build and manipulate DML statements for CSV data.
 */
public class CsvDMLWrapper implements AbstractDMLWrapper {

    private String kindName = null;
    private Map<String, String> rowData = new LinkedHashMap<>();

    @Override
    public void setKindName(String name) {
        this.kindName = name;
    }

    /**
     * Appends a new field to the DML statement with the given name and value.
     * If the value is null, it is stored as an empty string.
     *
     * @param name the name of the field.
     * @param value the value of the field; converted to a string.
     */
    @Override
    public void append(String name, Object value) {
        String stringValue = value == null ? "" : value.toString();
        rowData.put(name, stringValue);
    }

    /**
     * Creates a DML statement by joining the row data into a single CSV line.
     * The CSV data is properly escaped to handle commas and quotes.
     *
     * @return a {@link CsvCommandStatement} containing the generated CSV line.
     * @throws OtherException if an error occurs while creating the DML statement.
     */
    @Override
    public AbstractStatement createDMLStatement() {
        try {
            StringJoiner joiner = new StringJoiner(",");
            // Properly escape CSV data that may contain commas or quotes
            rowData.values().forEach(value -> joiner.add("\"" + value.replace("\"", "\"\"") + "\""));
            String csvLine = joiner.toString();
            return new CsvCommandStatement(csvLine);
        } catch (Exception e) {
            throw new OtherException(e);
        }
    }

    /**
     * Clears the current row data, resetting the state of the DML wrapper.
     */
    @Override
    public void clear() {
        rowData.clear();
    }

}
