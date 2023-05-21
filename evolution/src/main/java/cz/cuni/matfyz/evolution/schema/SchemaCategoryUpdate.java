package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.evolution.exception.SchemaEvolutionException;

import java.util.List;

public class SchemaCategoryUpdate {

    private final Version prevVersion;

    public Version getPrevVersion() {
        return prevVersion;
    }

    private final List<SchemaModificationOperation> operations;

    public SchemaCategoryUpdate(Version prevVersion, List<SchemaModificationOperation> operations) {
        this.prevVersion = prevVersion;
        this.operations = operations;
    }

    public DataResult<SchemaCategory> apply(SchemaCategory category) {
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
