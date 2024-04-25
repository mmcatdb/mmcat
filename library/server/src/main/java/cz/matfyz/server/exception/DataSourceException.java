package cz.matfyz.server.exception;

import cz.matfyz.abstractwrappers.datasource.DataSource.DataSourceType;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.datasource.DataSourceEntity;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class DataSourceException extends ServerException {

    private record Data(
        Id dataSourceId,
        DataSourceType type
    ) implements Serializable {}

    private DataSourceException(String name, DataSourceEntity dataSource, Throwable cause) {
        super("dataSource." + name, new Data(dataSource.id, dataSource.type), cause);
    }

    public static DataSourceException wrapperNotFound(DataSourceEntity dataSource) {
        return new DataSourceException("wrapperNotFound", dataSource, null);
    }

    public static DataSourceException wrapperNotCreated(DataSourceEntity dataSource, Throwable cause) {
        return new DataSourceException("wrapperNotCreated", dataSource, cause);
    }

}
