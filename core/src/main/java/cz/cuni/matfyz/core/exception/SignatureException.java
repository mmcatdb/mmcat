package cz.cuni.matfyz.core.exception;

/**
 * @author jachymb.bartik
 */
public class SignatureException extends CoreException {
    
    private SignatureException(String type, String value) {
        super("signature." + type, value, null);
    }

    public static SignatureException invalid(String value) {
        return new SignatureException("invalid", value);
    }

}
