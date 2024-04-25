package cz.matfyz.server.entity.datasource;

import cz.matfyz.abstractwrappers.datasource.DataSource.DataSourceType;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

public record DataSourceInfo (
    Id id,
    DataSourceType type,
    String label    
) implements IEntity {}
