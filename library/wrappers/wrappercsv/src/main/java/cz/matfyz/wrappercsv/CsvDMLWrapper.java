package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.exception.OtherException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class CsvDMLWrapper implements AbstractDMLWrapper {

    private String kindName = null;
    private Map<String, String> rowData = new LinkedHashMap<>();

    @Override
    public void setKindName(String name) {
        this.kindName = name;
    }

    @Override
    public void append(String name, Object value) {
        String stringValue = value == null ? "" : value.toString();
        rowData.put(name, stringValue);
    }

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

    @Override
    public void clear() {
        rowData.clear(); // reset
    }

}
