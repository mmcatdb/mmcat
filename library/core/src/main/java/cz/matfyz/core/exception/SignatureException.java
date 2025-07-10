package cz.matfyz.core.exception;

public class SignatureException extends CoreException {

    private SignatureException(String name, String value) {
        super("signature." + name, value, null);
    }

    public static SignatureException invalid(String value) {
        return new SignatureException("invalid", value);
    }

    public static SignatureException isEmpty() {
        return new SignatureException("isEmpty", "");
    }

}
