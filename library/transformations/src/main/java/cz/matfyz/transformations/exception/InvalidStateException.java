package cz.matfyz.transformations.exception;

import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
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

    public static InvalidStateException indexNotFound(Signature signature) {
        return new InvalidStateException("indexNotFound", signature);
    }

    public static InvalidStateException nameIsNotString(Name name) {
        return new InvalidStateException("nameIsNotString", name);
    }

    public static InvalidStateException complexPropertyForProperty(Key key) {
        return new InvalidStateException("complexPropertyForProperty", key);
    }

    public static InvalidStateException simplePropertyForEntity(AccessPath property) {
        return new InvalidStateException("simplePropertyForEntity", property);
    }

}
