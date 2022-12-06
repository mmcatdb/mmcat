package cz.cuni.matfyz.server.entity.logicalmodel;

import cz.cuni.matfyz.server.entity.IEntity;

/**
 * @author jachym.bartik
 */
public record LogicalModelInfo(
    int id,
    String jsonValue
) implements IEntity {}
