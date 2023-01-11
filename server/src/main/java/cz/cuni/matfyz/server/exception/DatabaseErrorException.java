package cz.cuni.matfyz.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author jachymb.bartik
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DatabaseErrorException extends RepositoryException {
    
    public DatabaseErrorException(String errorMessage) {
        super(errorMessage);
    }

    public DatabaseErrorException(String format, Object... arguments) {
        super(String.format(format, arguments));
    }

}
