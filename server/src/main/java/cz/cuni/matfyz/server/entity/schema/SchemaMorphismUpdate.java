package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.entity.Id;

public record SchemaMorphismUpdate(
    Id domId,
    Id codId,
    Integer temporaryDomId,
    Integer temporaryCodId,
    String jsonValue
) {}
