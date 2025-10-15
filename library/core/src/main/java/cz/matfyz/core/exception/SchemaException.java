package cz.matfyz.core.exception;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;

import java.io.Serializable;
import java.util.List;

public class SchemaException extends CoreException {

    protected SchemaException(String name, Serializable data) {
        super("schema." + name, data, null);
    }

    private record DependencyData(
        Key key,
        List<BaseSignature> signatures
    ) implements Serializable {}

    public static SchemaException removedObjexDependsOnMorphisms(Key key, List<BaseSignature> signatures) {
        return new SchemaException("removedObjexDependsOnMorphisms", new DependencyData(key, signatures));
    }

    public static SchemaException removingNonExistingObjex(Key key) {
        return new SchemaException("removingNonExistingObjex", key);
    }

    public static SchemaException replacingNonExistingObjex(Key key) {
        return new SchemaException("replacingNonExistingObjex", key);
    }

    public static SchemaException removingNonExistingMorphism(BaseSignature signature) {
        return new SchemaException("removingNonExistingMorphism", signature);
    }

}
