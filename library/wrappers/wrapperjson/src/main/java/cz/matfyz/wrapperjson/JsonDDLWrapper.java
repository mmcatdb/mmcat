package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

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

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        throw InvalidPathException.isSchemaless(DatasourceType.json, path);
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
