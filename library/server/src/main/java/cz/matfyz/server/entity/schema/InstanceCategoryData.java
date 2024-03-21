package cz.matfyz.server.entity.schema;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.MappingRow;

import java.util.List;
import java.util.Set;

public record InstanceCategoryData(
    List<InstanceObjectData> objects,
    List<InstanceMorphismData> morphisms
) {

    public record InstanceObjectData(
        Key key,
        List<DomainRow> rows
    ) {}

    public record InstanceMorphismData(
        Signature signature,
        Set<MappingRow> mappings
    ) {}

}
