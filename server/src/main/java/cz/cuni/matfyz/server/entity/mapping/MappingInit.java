package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.Id;

/**
 * @author jachym.bartik
 */
public record MappingInit(
    Id logicalModelId,
    Id rootObjectId,
    String jsonValue
) {}
