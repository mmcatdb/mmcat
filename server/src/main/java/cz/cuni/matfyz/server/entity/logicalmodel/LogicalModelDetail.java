package cz.cuni.matfyz.server.entity.logicalmodel;

import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.database.DatabaseWithConfiguration;
import cz.cuni.matfyz.server.entity.mapping.MappingDetail;

import java.util.List;

/**
 * @author jachym.bartik
 */
public record LogicalModelDetail(
    Id id,
    Id categoryId,
    DatabaseWithConfiguration database,
    String jsonValue,
    List<MappingDetail> mappings
) implements IEntity {}
