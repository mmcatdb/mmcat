package cz.matfyz.evolution.exception;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;

import java.io.Serializable;
import java.util.List;

public class DependencyException extends EvolutionException {

    private record DependencyData(
        Object entity,
        Object dependencies
    ) implements Serializable {}

    protected DependencyException(String name, Object entity, Object dependencies) {
        super("dependency." + name, new DependencyData(entity, dependencies), null);
    }

    public static DependencyException objexOnMorphisms(Key key, List<Signature> signatures) {
        return new DependencyException("objexOnMorphisms", key, signatures);
    }

}
