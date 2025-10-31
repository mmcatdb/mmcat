package cz.matfyz.core.exception;

import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name;

import java.io.Serializable;

public class AccessPathException extends CoreException {

    private AccessPathException(String name, Serializable data) {
        super("accessPath." + name, data, null);
    }

    private record Data(
        ComplexProperty property,
        Name tpye
    ) implements Serializable {}

    public static AccessPathException typedNameNotFound(ComplexProperty property, Name type) {
        return new AccessPathException("typedNameNotFound", new Data(property, type));
    }

    public static AccessPathException unsupportedName(Name name) {
        return new AccessPathException("unsupportedName", name);
    }

    public static AccessPathException multipleDynamicNamesWithoutPattern(ComplexProperty property) {
        return new AccessPathException("multipleDynamicNamesWithoutPattern", property);
    }

}
