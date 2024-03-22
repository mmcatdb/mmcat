package cz.matfyz.server.entity.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.MappingRepository.MappingJsonValue;

import java.util.List;

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

    public Mapping toMapping(SchemaCategory category) {
        return new Mapping(
            category,
            rootObjectKey,
            kindName,
            accessPath,
            List.of(primaryKey)
        );
    }

    @Override public final boolean equals(Object other) {
        return other instanceof IEntity iEntity && id.equals(iEntity.id());
    }

    @Override public final int hashCode() {
        return id.hashCode();
    }

    @Override public String toString() {
        return "Mapping: " + id;
    }

}
