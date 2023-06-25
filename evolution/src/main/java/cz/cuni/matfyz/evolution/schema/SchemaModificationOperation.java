package cz.cuni.matfyz.evolution.schema;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.evolution.exception.EvolutionException;

public interface SchemaModificationOperation {

    void apply(SchemaCategory category) throws EvolutionException;

}
