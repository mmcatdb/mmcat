package cz.cuni.matfyz.server.entity.logicalmodel;

import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.database.DatabaseWithConfiguration;
import cz.cuni.matfyz.server.entity.mapping.MappingWrapper;

import java.util.List;

/**
 * @author jachym.bartik
 */
public record LogicalModelFull(
    int id,
    int categoryId,
    DatabaseWithConfiguration database,
    String jsonValue,
    List<MappingWrapper> mappings
) implements IEntity {}
