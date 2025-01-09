package cz.matfyz.core.exception;

import cz.matfyz.core.identifiers.Signature;

import java.io.Serializable;

public class AccessPathException extends CoreException {

    private AccessPathException(String name, Serializable data) {
        super("accessPath." + name, data, null);
    }

    private record Data(
        Signature dynamicName,
        Signature property
    ) implements Serializable {}

    public static AccessPathException dynamicNameMissingCommonPrefix(Signature dynamicName, Signature property) {
        return new AccessPathException("dynamicNameMissingCommonPrefix", new Data(dynamicName, property));
    }

}
