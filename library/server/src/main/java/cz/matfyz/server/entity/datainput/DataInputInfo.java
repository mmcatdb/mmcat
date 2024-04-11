package cz.matfyz.server.entity.datainput;

import cz.matfyz.server.entity.datasource.DataSource;
import cz.matfyz.abstractwrappers.database.Database.DatabaseType;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

public record DataInputInfo (
    Id id,
    DatabaseType databaseType,
    DataSource.Type dataSourceType,
    String label    
) implements IEntity {}
