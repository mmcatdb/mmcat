package cz.matfyz.evolution.exception;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;

import java.io.Serializable;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class DependencyException extends EvolutionException {

    private record DependencyData(
        String type,
        Object entity,
        Object dependencies
    ) implements Serializable {}

    protected DependencyException(String type, Object entity, Object dependencies) {
        super("dependency", new DependencyData(type, entity, dependencies), null);
    }

    public static DependencyException objectOnMorphisms(Key key, List<Signature> signatures) {
        return new DependencyException("objectOnMorphisms", key, signatures);
    }

}
