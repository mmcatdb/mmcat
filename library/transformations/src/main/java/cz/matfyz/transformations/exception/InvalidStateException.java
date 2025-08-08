package cz.matfyz.transformations.exception;

import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.Name;

import java.io.Serializable;

public class InvalidStateException extends TransformationException {

    private InvalidStateException(String name, Serializable data) {
        super("invalidState." + name, data, null);
    }

    public static InvalidStateException dynamicNameNotFound(DynamicName dynamicName) {
        return new InvalidStateException("dynamicNameNotFound", dynamicName);
    }

    public static InvalidStateException nameIsNotStatic(Name name) {
        return new InvalidStateException("nameIsNotStatic", name);
    }

    public static InvalidStateException complexPropertyForValueIds(Key key) {
        return new InvalidStateException("complexPropertyForValueIds", key);
    }

    public static InvalidStateException simplePropertyForNonValueIds(AccessPath property) {
        return new InvalidStateException("simplePropertyForNonValueIds", property);
    }

}
