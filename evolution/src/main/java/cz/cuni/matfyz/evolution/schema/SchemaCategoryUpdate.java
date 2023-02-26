package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.evolution.exception.SchemaEvolutionException;

import java.util.List;

import com.fasterxml.jackson.core.Version;

public class SchemaCategoryUpdate {

    private final Version beforeVersion;

    public Version getBeforeVersion() {
        return beforeVersion;
    }

    private final List<SchemaModificationOperation> operations;

    public SchemaCategoryUpdate(Version beforeVersion, List<SchemaModificationOperation> operations) {
        this.beforeVersion = beforeVersion;
        this.operations = operations;
    }

    public DataResult<SchemaCategory> apply(SchemaCategory originalCategory) {
        final var category = originalCategory.clone();

        for (final var operation : operations) {
            try {
                operation.apply(category);
            }
            catch (SchemaEvolutionException exception) {
                return new DataResult<>(null, exception.getMessage());
            }
        }

        return new DataResult<>(category);
    }

}
