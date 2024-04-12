package cz.matfyz.server.entity.datainput;

import cz.matfyz.abstractwrappers.datainput.DataInput.DataInputType;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

public record DataInputInfo (
    Id id,
    DataInputType type,
    String label    
) implements IEntity {}
