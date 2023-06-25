package cz.cuni.matfyz.evolution.mapping;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.evolution.exception.EvolutionException;

public interface MappingModificationOperation {

    void apply(Mapping mapping) throws EvolutionException;

}
