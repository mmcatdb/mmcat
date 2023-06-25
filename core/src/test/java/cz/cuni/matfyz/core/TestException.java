package cz.cuni.matfyz.core;

import cz.cuni.matfyz.core.exception.NamedException;

import java.io.Serializable;

/**
 * A testing exception for json serialization tests.
 * @author jachymb.bartik
 */
public class TestException extends NamedException {

    public TestException(String name, Serializable data, Throwable cause) {
        super("test." + name, data, cause);
    }

}
