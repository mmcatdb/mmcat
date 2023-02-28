package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.evolution.exception.SchemaEvolutionException;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SchemaCategoryUpdate {

    private final Version beforeVersion;

    public Version getBeforeVersion() {
        return beforeVersion;
    }

    private final List<SchemaModificationOperation> operations;

    @JsonCreator
    public SchemaCategoryUpdate(
        @JsonProperty("beforeVersion") Version beforeVersion,
        @JsonProperty("operations") List<SchemaModificationOperation> operations
    ) {
        this.beforeVersion = beforeVersion;
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
