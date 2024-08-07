package cz.matfyz.server.entity.logicalmodel;

import cz.matfyz.server.entity.Id;

public record LogicalModelInit(
    Id datasourceId,
    Id categoryId,
    String label
) {}
