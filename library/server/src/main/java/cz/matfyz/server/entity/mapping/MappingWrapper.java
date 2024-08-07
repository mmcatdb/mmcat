package cz.matfyz.server.entity.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.MappingRepository.MappingJsonValue;

import java.util.List;

public class MappingWrapper extends Entity {

    public final Id logicalModelId;
    public final Key rootObjectKey;
    public final List<Signature> primaryKey;
    public final String kindName;
    public final ComplexProperty accessPath;
    public final Version version;

    public MappingWrapper(
        Id id,
        Id logicalModelId,
        Key rootObjectKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath,
        Version version
    ) {
        super(id);

        this.logicalModelId = logicalModelId;
        this.rootObjectKey = rootObjectKey;
        this.primaryKey = primaryKey;
        this.kindName = kindName;
        this.accessPath = accessPath;
        this.version = version;
    }

    public static MappingWrapper fromJsonValue(Id id, Id logicalModelId, MappingJsonValue jsonValue) {
        return new MappingWrapper(
            id,
            logicalModelId,
            jsonValue.rootObjectKey(),
            jsonValue.primaryKey(),
            jsonValue.kindName(),
            jsonValue.accessPath(),
            jsonValue.version()
        );
    }

    public MappingJsonValue toJsonValue() {
        return new MappingJsonValue(
            rootObjectKey,
            primaryKey,
            kindName,
            accessPath,
            version
        );
    }

    public Mapping toMapping(SchemaCategory category) {
        return new Mapping(
            category,
            rootObjectKey,
            kindName,
            accessPath,
            primaryKey
        );
    }

}
