package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

/**
 * A Data Definition Language (DDL) wrapper for JSON that implements the {@link AbstractDDLWrapper} interface.
 * This class provides methods to define and manage JSON schema properties and create DDL statements for JSON data.
 */
public class JsonDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaless() {
        return true;
    }

    /**
     * Attempts to add a simple property to the JSON schema. This operation is not supported
     * for JSON in this implementation and always returns false.
     *
     * @param path the path of the property.
     * @param required whether the property is required (not used in JSON).
     * @return false, as this operation is not supported.
     */
    @Override public boolean addSimpleProperty(String path, boolean required) {
        return false;
    }

    /**
     * Attempts to add a simple array property to the JSON schema. This operation is not supported
     * for JSON in this implementation and always returns false.
     *
     * @param path the path of the property.
     * @param required whether the property is required (not used in JSON).
     * @return false, as this operation is not supported.
     */
    @Override public boolean addSimpleArrayProperty(String path, boolean required) {
      return false;
    }

    /**
     * Attempts to add a complex property to the JSON schema. This operation is not supported
     * for JSON in this implementation and always returns false.
     *
     * @param path the path of the property.
     * @param required whether the property is required (not used in JSON).
     * @return false, as this operation is not supported.
     */
    @Override public boolean addComplexProperty(String path, boolean required) {
        return false;
    }

    /**
     * Attempts to add a complex array property to the JSON schema. This operation is not supported
     * for JSON in this implementation and always returns false.
     *
     * @param path the path of the property.
     * @param required whether the property is required (not used in JSON).
     * @return false, as this operation is not supported.
     */
    @Override public boolean addComplexArrayProperty(String path, boolean required) {
       return false;
    }

    /**
     * Creates a DDL statement for the JSON schema.
     *
     * @return a {@link JsonCommandStatement} containing the generated DDL statement.
     */
    @Override public JsonCommandStatement createDDLStatement() {
        return new JsonCommandStatement("");
    }
}
