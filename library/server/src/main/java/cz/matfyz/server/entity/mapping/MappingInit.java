package cz.matfyz.server.entity.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.MappingRepository;
import cz.matfyz.server.repository.MappingRepository.MappingJsonValue;

public record MappingInit(
    Id logicalModelId,
    Key rootObjectKey,
    Signature[] primaryKey,
    String kindName,
    ComplexProperty accessPath
) {

    public MappingJsonValue toJsonValue(Version version) {
        return new MappingRepository.MappingJsonValue(
            rootObjectKey,
            primaryKey,
            kindName,
            accessPath,
            version
        );
    }

}
