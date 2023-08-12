package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.exception.EvolutionException;

public interface SchemaModificationOperation {

    void apply(SchemaCategory category) throws EvolutionException;

}
