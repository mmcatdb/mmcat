package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.entity.Id;

public record SchemaMorphismUpdateFixed(
    Id domId,
    Id codId,
    String jsonValue
) {}
