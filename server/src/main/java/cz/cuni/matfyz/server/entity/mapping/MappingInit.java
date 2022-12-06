package cz.cuni.matfyz.server.entity.mapping;

/**
 * @author jachym.bartik
 */
public record MappingInit(
    int logicalModelId,
    int rootObjectId,
    String mappingJsonValue,
    String jsonValue
) {}
