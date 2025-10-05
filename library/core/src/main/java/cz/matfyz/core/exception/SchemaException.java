package cz.matfyz.core.exception;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;

import java.io.Serializable;
import java.util.List;

public class SchemaException extends CoreException {

    protected SchemaException(String name, Serializable data) {
        super("schema." + name, data, null);
    }

    private record DependencyData(
        Key key,
        List<Signature> signatures
    ) implements Serializable {}

    public static SchemaException removedObjexDependsOnMorphisms(Key key, List<Signature> signatures) {
        return new SchemaException("removedObjexDependsOnMorphisms", new DependencyData(key, signatures));
    }

    public static SchemaException removingNonExistingObjex(Key key) {
        return new SchemaException("removingNonExistingObjex", key);
    }

    public static SchemaException replacingNonExistingObjex(Key key) {
        return new SchemaException("replacingNonExistingObjex", key);
    }

}
