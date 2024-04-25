package cz.matfyz.server.entity.datasource;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

public record DatasourceInfo (
    Id id,
    DatasourceType type,
    String label    
) implements IEntity {}
