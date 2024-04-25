package cz.matfyz.server.entity.datasource;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public class DatasourceWithConfiguration {

    public final Id id;
    public final DatasourceType type;
    public final String label;
    public final DatasourceConfiguration configuration;

    public DatasourceWithConfiguration(DatasourceWrapper datasource, DatasourceConfiguration configuration) {
        this.id = datasource.id;
        this.type = datasource.type;
        this.label = datasource.label;
        this.configuration = configuration;
    }

}

