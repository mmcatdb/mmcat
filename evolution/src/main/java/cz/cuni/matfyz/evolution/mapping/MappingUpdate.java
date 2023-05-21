package cz.cuni.matfyz.evolution.mapping;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.evolution.exception.MappingEvolutionException;

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

    public DataResult<Mapping> apply(Mapping originalCategory) {
        final var category = originalCategory.clone();

        for (final var operation : operations) {
            try {
                operation.apply(category);
            }
            catch (MappingEvolutionException exception) {
                return new DataResult<>(null, exception.getMessage());
            }
        }

        return new DataResult<>(category);
    }

}
