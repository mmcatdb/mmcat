package cz.cuni.matfyz.server.entity.mapping;

/**
 * @author jachym.bartik
 */
public record MappingInit(
    int logicalModelId,
    Integer rootObjectId,
    Integer rootMorphismId,
    String mappingJsonValue,
    String jsonValue
) {}
