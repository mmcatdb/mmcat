package cz.matfyz.server.entity.mapping;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.schema.Key;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.MappingRepository.MappingJsonValue;

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
