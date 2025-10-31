package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.Collection;
import java.util.List;

/**
 * A Data Definition Language (DDL) wrapper for JSON that implements the {@link AbstractDDLWrapper} interface.
 * This class provides methods to define and manage JSON schema properties and create DDL statements for JSON data.
 */
public class JsonDDLWrapper implements AbstractDDLWrapper {

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public void clear() {
        kindName = null;
    }

    private String kindName = null;

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        throw InvalidPathException.isSchemaless(DatasourceType.json, path);
    }

    /**
     * Creates a DDL statement for the JSON schema.
     */
    @Override public AbstractStatement createDDLStatement() {
        return AbstractStatement.createEmpty();
    }

    @Override
    public Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands) {
        throw new UnsupportedOperationException("Unimplemented method 'createDDLDeleteStatements'");
    }

    @Override
    public AbstractStatement createCreationStatement(String newDBName, String owner) {
        throw new UnsupportedOperationException("Unimplemented method 'createCreationStatement'");
    }
}
