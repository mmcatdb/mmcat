package cz.matfyz.server.entity.mapping;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.IEntity;
import cz.matfyz.server.entity.Id;

public record MappingInfo(
    Id id,
    String kindName,
    Version version
) implements IEntity {

    public static MappingInfo fromWrapper(MappingWrapper wrapper) {
        return new MappingInfo(wrapper.id(), wrapper.kindName, wrapper.version);
    }

}
