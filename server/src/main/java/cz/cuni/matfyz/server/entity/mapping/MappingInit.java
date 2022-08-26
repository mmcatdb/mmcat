package cz.cuni.matfyz.server.entity.mapping;

/**
 * @author jachym.bartik
 */
public record MappingInit(
    int databaseId,
    int categoryId,
    Integer rootObjectId,
    Integer rootMorphismId,
    String mappingJsonValue,
    String jsonValue
) {}
