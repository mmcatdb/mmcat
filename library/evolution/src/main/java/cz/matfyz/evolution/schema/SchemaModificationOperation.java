package cz.matfyz.evolution.schema;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.evolution.exception.EvolutionException;

public interface SchemaModificationOperation {

    void up(SchemaCategory category) throws EvolutionException;

    void down(SchemaCategory category) throws EvolutionException;

    <T> T accept(SchemaEvolutionVisitor<T> visitor) throws EvolutionException;
}
