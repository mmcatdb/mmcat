package cz.matfyz.abstractwrappers.exception;

import cz.matfyz.core.exception.NamedException;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class WrapperException extends NamedException {

    protected WrapperException(String name, @Nullable Serializable data, @Nullable Throwable cause) {
        super("wrapper." + name, data, cause);
    }

}
