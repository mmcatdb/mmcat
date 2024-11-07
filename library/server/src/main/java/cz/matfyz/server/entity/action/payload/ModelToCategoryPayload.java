package cz.matfyz.server.entity.action.payload;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.action.ActionPayload;

import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public record ModelToCategoryPayload(
    Id datasourceId,
    /** If provided, only the selected mappings from this datasource will be used. */
    @Nullable List<Id> mappingIds
) implements ActionPayload {}
