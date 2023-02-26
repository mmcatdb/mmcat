package cz.cuni.matfyz.server.entity.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.repository.MappingRepository;
import cz.cuni.matfyz.server.repository.MappingRepository.MappingJsonValue;

/**
 * @author jachym.bartik
 */
public record MappingInit(
    Id logicalModelId,
    Key rootObjectKey,
    Signature[] primaryKey,
    String kindName,
    ComplexProperty accessPath,
    Version categoryVersion
) {
    public MappingJsonValue toJsonValue() {
        return new MappingRepository.MappingJsonValue(
            rootObjectKey,
            primaryKey,
            kindName,
            accessPath,
            categoryVersion,
            Version.generateInitial()
        );
    }
}
