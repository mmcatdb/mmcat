package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record MappingInfo(
    Id id,
    String kindName,
    Version version,
    Version categoryVersion
) implements IEntity {}
