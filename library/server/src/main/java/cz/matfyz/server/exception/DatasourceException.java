package cz.matfyz.server.exception;

import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceEntity;

import java.io.Serializable;

public class DatasourceException extends ServerException {

    private record Data(
        Id datasourceId,
        DatasourceType type
    ) implements Serializable {}

    private DatasourceException(String name, DatasourceEntity datasource, Throwable cause) {
        super("datasource." + name, new Data(datasource.id(), datasource.type), cause);
    }

    public static DatasourceException wrapperNotFound(DatasourceEntity datasource) {
        return new DatasourceException("wrapperNotFound", datasource, null);
    }

    public static DatasourceException wrapperNotCreated(DatasourceEntity datasource, Throwable cause) {
        return new DatasourceException("wrapperNotCreated", datasource, cause);
    }

}
