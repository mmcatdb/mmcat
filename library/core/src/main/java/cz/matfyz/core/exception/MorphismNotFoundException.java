package cz.matfyz.core.exception;

import cz.matfyz.core.category.BaseSignature;
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

    public static MorphismNotFoundException signatureIsEmpty() {
        return new MorphismNotFoundException("signatureIsEmpty", Signature.createEmpty());
    }

    public static MorphismNotFoundException baseNotFound(BaseSignature signature) {
        return new MorphismNotFoundException("baseNotFound", signature);
    }

}
