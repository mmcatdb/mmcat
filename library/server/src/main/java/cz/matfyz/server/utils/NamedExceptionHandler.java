package cz.matfyz.server.utils;

import cz.matfyz.core.exception.NamedException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class NamedExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { NamedException.class })
    protected ResponseEntity<Object> handleConflict(NamedException exception, WebRequest request) {
        return handleExceptionInternal(exception, exception.toSerializedException(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
