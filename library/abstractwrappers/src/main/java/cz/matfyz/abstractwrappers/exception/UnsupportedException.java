package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;

import java.io.Serializable;

public class UnsupportedException extends WrapperException {

    private record UnsupportedData(
        DatasourceType type,
        String method
    ) implements Serializable {}

    private UnsupportedException(DatasourceType type, String method) {
        super("unsupported", new UnsupportedData(type, method), null);
    }

    public static UnsupportedException addSimpleProperty(DatasourceType type) {
        return new UnsupportedException(type, "addSimpleProperty");
    }

    public static UnsupportedException addSimpleArrayProperty(DatasourceType type) {
        return new UnsupportedException(type, "addSimpleArrayProperty");
    }

    public static UnsupportedException addComplexProperty(DatasourceType type) {
        return new UnsupportedException(type, "addComplexProperty");
    }

    public static UnsupportedException addComplexArrayProperty(DatasourceType type) {
        return new UnsupportedException(type, "addComplexArrayProperty");
    }

}
