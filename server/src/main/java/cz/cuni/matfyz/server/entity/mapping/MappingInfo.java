package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record MappingInfo(
    Id id,
    String kindName
) implements IEntity {}
