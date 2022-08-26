package cz.cuni.matfyz.server.entity.schema;

public record SchemaMorphismUpdateFixed(
    Integer domId,
    Integer codId,
    String jsonValue
) {}
