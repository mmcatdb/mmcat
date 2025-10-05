package cz.matfyz.core.exception;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;

import java.io.Serializable;

public class MorphismNotFoundException extends CoreException {

    private MorphismNotFoundException(String name, Serializable data) {
        super("morphismNotFound." + name, data, null);
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

    private record MessageData(String message) implements Serializable {}

    /** Use only as a last resord! */
    public static MorphismNotFoundException withMessage(String message) {
        return new MorphismNotFoundException("withMessage", new MessageData(message));
    }
}
