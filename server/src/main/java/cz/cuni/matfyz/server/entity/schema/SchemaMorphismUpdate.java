package cz.cuni.matfyz.server.entity.schema;

public record SchemaMorphismUpdate(
    Integer domId,
    Integer codId,
    Integer temporaryDomId,
    Integer temporaryCodId,
    String jsonValue
) {}
