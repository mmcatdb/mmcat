package cz.matfyz.core.exception;

import java.io.Serializable;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A base class for all core exceptions.
 */
public abstract class CoreException extends NamedException {

    protected CoreException(String name, Serializable data, @Nullable Throwable cause) {
        super("core." + name, data, cause);
    }

}
