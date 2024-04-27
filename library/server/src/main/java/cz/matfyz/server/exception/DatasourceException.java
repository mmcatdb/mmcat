package cz.matfyz.server.exception;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DatasourceWrapper;

import java.io.Serializable;

public class DatasourceException extends ServerException {

    private record Data(
        Id datasourceId,
        DatasourceType type
    ) implements Serializable {}

    private DatasourceException(String name, DatasourceWrapper datasource, Throwable cause) {
        super("datasource." + name, new Data(datasource.id, datasource.type), cause);
    }

    public static DatasourceException wrapperNotFound(DatasourceWrapper datasource) {
        return new DatasourceException("wrapperNotFound", datasource, null);
    }

    public static DatasourceException wrapperNotCreated(DatasourceWrapper datasource, Throwable cause) {
        return new DatasourceException("wrapperNotCreated", datasource, cause);
    }

}
