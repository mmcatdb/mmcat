package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

public class Neo4jDDLWrapper implements AbstractDDLWrapper {

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
        throw InvalidPathException.isSchemaless(DatasourceType.neo4j, path);
    }

    @Override public AbstractStatement createDDLStatement() {
        return AbstractStatement.createEmpty();
    }

}
