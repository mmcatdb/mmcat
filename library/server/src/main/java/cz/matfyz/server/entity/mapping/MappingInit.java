package cz.matfyz.server.entity.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.server.entity.Id;

import java.util.List;

public record MappingInit(
    Id categoryId,
    Id datasourceId,
    Key rootObjectKey,
    List<Signature> primaryKey,
    String kindName,
    ComplexProperty accessPath
) {

    public static MappingInit fromMapping(Mapping mapping, Id categoryId, Id datasourceId) {
        return new MappingInit(
            categoryId,
            datasourceId,
            mapping.rootObject().key(),
            mapping.primaryKey().stream().toList(),
            mapping.kindName(),
            mapping.accessPath()
        );
    }

}
