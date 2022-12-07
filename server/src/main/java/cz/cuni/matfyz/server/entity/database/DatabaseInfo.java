package cz.cuni.matfyz.server.entity.database;

import cz.cuni.matfyz.server.entity.IEntity;

/**
 * @author jachym.bartik
 */
public record DatabaseInfo(
    int id,
    Database.Type type,
    String label
) implements IEntity {}
