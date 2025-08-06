package cz.matfyz.core.exception;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;

public class MorphismNotFoundException extends CoreException {

    private MorphismNotFoundException(String name, Signature signature) {
        super("morphismNotFound." + name, signature, null);
    }

    public static MorphismNotFoundException signatureIsDual(Signature signature) {
        return new MorphismNotFoundException("signatureIsDual", signature);
    }

    public static MorphismNotFoundException signatureIsEmpty() {
        return new MorphismNotFoundException("signatureIsEmpty", Signature.empty());
    }

    public static MorphismNotFoundException signatureIsComposite(Signature signature) {
        return new MorphismNotFoundException("signatureIsComposite", signature);
    }

    public static MorphismNotFoundException baseNotFound(BaseSignature signature) {
        return new MorphismNotFoundException("baseNotFound", signature);
    }

}
