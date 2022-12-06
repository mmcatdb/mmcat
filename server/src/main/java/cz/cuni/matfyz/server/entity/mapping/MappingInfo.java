package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.server.entity.IEntity;

/**
 * @author jachym.bartik
 */
public record MappingInfo(
    int id,
    String jsonValue

) implements IEntity {}
