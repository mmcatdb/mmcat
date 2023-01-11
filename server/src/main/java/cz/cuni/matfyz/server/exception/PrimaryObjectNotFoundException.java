package cz.cuni.matfyz.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author jachymb.bartik
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PrimaryObjectNotFoundException extends RepositoryException {
    
    public PrimaryObjectNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    public PrimaryObjectNotFoundException(String format, Object... arguments) {
        super(String.format(format, arguments));
    }

}
