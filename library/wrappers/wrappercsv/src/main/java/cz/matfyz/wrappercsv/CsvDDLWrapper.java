package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * A Data Definition Language (DDL) wrapper for CSV that implements the {@link AbstractDDLWrapper} interface.
 * This class provides methods to define and manage CSV schema properties and create DDL statements for CSV data.
 */
public class CsvDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;
    private List<String> properties = new ArrayList<>();

    public void setKindName(String name) {
        kindName = name;
    }

    @Override
    public boolean isSchemaless() {
        return true;
    }

    /**
     * Adds a simple property to the CSV schema. Since the CSV structure is flat,
     * the path should be equal to the simple name of the property.
     *
     * @param path the name of the property.
     * @param required whether the property is required (not used in CSV).
     * @return true if the property was successfully added.
     */
    @Override
    public boolean addSimpleProperty(String path, boolean required) {
        // The CSV structure is flat, therefore, the path should be equal to the simple name of the property.
        properties.add(path);
        return true;
    }

    /**
     * Attempts to add a simple array property to the CSV schema. This operation is not
     * supported for CSV and always returns false.
     *
     * @param path the path of the property.
     * @param required whether the property is required (not used in CSV).
     * @return false, as this operation is not supported.
     */
    @Override
    public boolean addSimpleArrayProperty(String path, boolean required) {
        return false;
    }

    /**
     * Attempts to add a complex property to the CSV schema. This operation is not
     * supported for CSV and always returns false.
     *
     * @param path the path of the property.
     * @param required whether the property is required (not used in CSV).
     * @return false, as this operation is not supported.
     */
    @Override
    public boolean addComplexProperty(String path, boolean required) {
        return false;
    }

    /**
     * Attempts to add a complex array property to the CSV schema. This operation is not
     * supported for CSV and always returns false.
     *
     * @param path the path of the property.
     * @param required whether the property is required (not used in CSV).
     * @return false, as this operation is not supported.
     */
    @Override
    public boolean addComplexArrayProperty(String path, boolean required) {
        return false;
    }

    /**
     * Creates a DDL statement for the CSV schema by generating a header line with the
     * specified properties.
     *
     * @return a {@link CsvCommandStatement} containing the generated DDL statement as a CSV header line.
     */
    @Override
    public CsvCommandStatement createDDLStatement() {
        final String headerLine = String.join(",", properties);
        return new CsvCommandStatement(headerLine);
    }
}
