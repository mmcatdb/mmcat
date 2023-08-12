package cz.matfyz.server.entity.logicalmodel;

import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.database.DatabaseWithConfiguration;
import cz.matfyz.server.entity.mapping.MappingWrapper;

import java.util.List;

/**
 * @author jachym.bartik
 */
public record LogicalModelDetail(
    Id id,
    Id categoryId,
    DatabaseWithConfiguration database,
    String label,
    List<MappingWrapper> mappings
) implements IEntity {}
