package cz.matfyz.querying.exception;

import cz.matfyz.core.schema.SchemaObject;

import java.io.Serializable;
import java.util.List;

/**
 * @author jachymb.bartik
 */
public class ProjectionException extends QueryingException {

    protected ProjectionException(String name, Serializable data) {
        super("projection." + name, data, null);
    }

    public static ProjectionException notTree() {
        return new ProjectionException("notTree", null);
    }

    private record NotSingleRootData(
        List<SchemaObject> selectRoots
    ) implements Serializable {}

    public static ProjectionException notSingleRoot(List<SchemaObject> selectRoots) {
        return new ProjectionException("notSingleRoot", new NotSingleRootData(selectRoots));
    }

    private record PathNotFoundData(
        SchemaObject source,
        SchemaObject target
    ) implements Serializable {}

    public static ProjectionException PathNotFound(SchemaObject source, SchemaObject target) {
        return new ProjectionException("pathNotFound", new PathNotFoundData(source, target));
    }

}
