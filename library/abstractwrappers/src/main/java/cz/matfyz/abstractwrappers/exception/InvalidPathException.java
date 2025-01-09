package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper.PropertyPath;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.io.Serializable;

public class InvalidPathException extends WrapperException {

    private record InvalidPathData(
        DatasourceType datasource,
        PropertyPath path
    ) implements Serializable {}

    protected InvalidPathException(String name, DatasourceType datasource, PropertyPath path) {
        super("invalidPath." + name, new InvalidPathData(datasource, path), null);
    }

    public static InvalidPathException isSchemaless(DatasourceType datasource, PropertyPath path) {
        return new InvalidPathException("isSchemaless", datasource, path);
    }

    public static InvalidPathException wrongLength(DatasourceType datasource, PropertyPath path) {
        return new InvalidPathException("wrongLength", datasource, path);
    }

    public static InvalidPathException isComplex(DatasourceType datasource, PropertyPath path) {
        return new InvalidPathException("isComplex", datasource, path);
    }

    public static InvalidPathException isArray(DatasourceType datasource, PropertyPath path) {
        return new InvalidPathException("isArray", datasource, path);
    }

}
