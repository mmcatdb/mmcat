package cz.matfyz.server.exception;

import cz.matfyz.core.exception.NamedException;

import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A base class for all server exceptions.
 * @author jachymb.bartik
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public abstract class ServerException extends NamedException {

    protected ServerException(String name, Serializable data, Throwable cause) {
        super("server." + name, data, cause);
    }

}
