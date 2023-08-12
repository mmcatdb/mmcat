package cz.matfyz.server.entity.logicalmodel;

import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record LogicalModelInfo(
    Id id,
    String label
) implements IEntity {}
