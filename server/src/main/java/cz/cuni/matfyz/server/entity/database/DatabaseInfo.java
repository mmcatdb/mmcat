package cz.cuni.matfyz.server.entity.database;

import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record DatabaseInfo(
    Id id,
    Database.Type type,
    String label
) implements IEntity {}
