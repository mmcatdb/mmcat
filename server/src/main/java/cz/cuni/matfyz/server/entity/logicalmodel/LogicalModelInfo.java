package cz.cuni.matfyz.server.entity.logicalmodel;

import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record LogicalModelInfo(
    Id id,
    String jsonValue
) implements IEntity {}
