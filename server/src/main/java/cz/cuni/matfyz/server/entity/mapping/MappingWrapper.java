package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.entity.IEntity;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.repository.MappingRepository.MappingJsonValue;

/**
 * @author jachym.bartik
 */
public record MappingWrapper(
    Id id,
    Id logicalModelId,
    Key rootObjectKey,
    Signature[] primaryKey,
    String kindName,
    ComplexProperty accessPath,
    Version version,
    Version categoryVersion
) implements IEntity {

    public MappingWrapper(
        Id id,
        Id logicalModelId,
        MappingJsonValue jsonValue
    ) {
        this(
            id,
            logicalModelId,
            jsonValue.rootObjectKey(),
            jsonValue.primaryKey(),
            jsonValue.kindName(),
            jsonValue.accessPath(),
            jsonValue.version(),
            jsonValue.categoryVersion()
        );
    }

}
