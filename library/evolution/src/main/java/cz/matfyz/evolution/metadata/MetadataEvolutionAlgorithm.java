package cz.matfyz.evolution.metadata;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.evolution.Version;

import java.util.List;

public class MetadataEvolutionAlgorithm {

    private final Version prevVersion;

    public Version getPrevVersion() {
        return prevVersion;
    }

    public final List<MMO> operations;

    public MetadataEvolutionAlgorithm(Version prevVersion, List<MMO> operations) {
        this.prevVersion = prevVersion;
        this.operations = operations;
    }

    public MetadataCategory up(MetadataCategory metadata) {
        for (final var operation : operations)
            operation.up(metadata);

        return metadata;
    }

    public MetadataCategory down(MetadataCategory metadata) {
        for (final var operation : operations.reversed())
            operation.down(metadata);

        return metadata;
    }

}
