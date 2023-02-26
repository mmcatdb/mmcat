package cz.cuni.matfyz.evolution.mapping;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.evolution.exception.MappingEvolutionException;

import java.util.List;

public class MappingUpdate {

    private final int beforeVersion;

    public int getBeforeVersion() {
        return beforeVersion;
    }

    private final List<MappingModificationOperation> operations;

    public MappingUpdate(int beforeVersion, List<MappingModificationOperation> operations) {
        this.beforeVersion = beforeVersion;
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
