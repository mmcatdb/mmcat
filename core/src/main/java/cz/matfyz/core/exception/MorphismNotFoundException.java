package cz.matfyz.core.exception;

import cz.matfyz.core.category.Signature;

/**
 * @author jachymb.bartik
 */
public class MorphismNotFoundException extends CoreException {
    
    private MorphismNotFoundException(String name, Signature signature) {
        super("morphismNotFound." + name, signature, null);
    }

    public static MorphismNotFoundException signatureIsDual(Signature signature) {
        return new MorphismNotFoundException("signatureIsDual", signature);
    }

    public static MorphismNotFoundException baseNotFound(Signature signature) {
        return new MorphismNotFoundException("baseNotFound", signature);
    }

}
