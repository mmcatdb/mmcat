package cz.matfyz.evolution.mapping;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.evolution.exception.EvolutionException;

public interface MappingModificationOperation {

    void apply(Mapping mapping) throws EvolutionException;

}
