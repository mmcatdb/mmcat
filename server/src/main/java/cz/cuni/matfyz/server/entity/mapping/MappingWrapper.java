package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;

/**
 * @author jachym.bartik
 */
public record MappingWrapper(
    Id id,
    Id logicalModelId,
    SchemaObjectWrapper rootObject,
    Signature[] primaryKey,
    String kindName,
    ComplexProperty accessPath
) implements IEntity {}
