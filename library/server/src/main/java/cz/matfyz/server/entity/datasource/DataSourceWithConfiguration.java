package cz.matfyz.server.entity.datasource;

import cz.matfyz.abstractwrappers.datasource.DataSource.DataSourceType;
import cz.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class DataSourceWithConfiguration {

    public final Id id;
    public final DataSourceType type;
    public final String label;
    public final DataSourceConfiguration configuration;

    public DataSourceWithConfiguration(DataSourceEntity dataSource, DataSourceConfiguration configuration) {
        this.id = dataSource.id;
        this.type = dataSource.type;
        this.label = dataSource.label;
        this.configuration = configuration;
    }

}

