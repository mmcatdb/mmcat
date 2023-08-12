package cz.matfyz.integration.exception;

import cz.matfyz.core.schema.Key;

import java.io.Serializable;

/**
 * @author jachymb.bartik
 */
public class MorphismException extends IntegrationException {
    
    private record MorphismData(
        String pimIri,
        Key object
    ) implements Serializable {}
    
    protected MorphismException(String name, MorphismData data) {
        super("morphism." + name, data, null);
    }

    public static MorphismException notFound(String pimIri) {
        return new MorphismException("notFound", new MorphismData(pimIri, null));
    }

    public static MorphismException multipleFound(Key object) {
        return new MorphismException("multipleFound", new MorphismData(null, object));
    }

    public static MorphismException multipleDirectFound(String pimIri, Key object) {
        return new MorphismException("multipleDirectFound", new MorphismData(pimIri, object));
    }

}
