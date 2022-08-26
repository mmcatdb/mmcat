package cz.cuni.matfyz.server.entity.schema;

import cz.cuni.matfyz.server.utils.Position;

public record SchemaObjectUpdate(
    int temporaryId,
    Position position,
    String jsonValue
) {}
