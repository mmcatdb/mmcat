package cz.matfyz.server.entity.database;

import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record DatabaseInfo(
    Id id,
    Database.Type type,
    String label
) implements IEntity {}
