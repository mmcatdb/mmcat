package cz.cuni.matfyz.evolution.mapping;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.evolution.Version;

import java.util.List;

public class MappingUpdate {

    private final Version prevVersion;

    public Version getPrevVersion() {
        return prevVersion;
    }

    private final List<MappingModificationOperation> operations;

    public MappingUpdate(Version prevVersion, List<MappingModificationOperation> operations) {
        this.prevVersion = prevVersion;
        this.operations = operations;
    }

    public Mapping apply(Mapping originalMapping) {
        final var mapping = originalMapping.clone();

        for (final var operation : operations)
            operation.apply(mapping);

        return mapping;
    }

}
