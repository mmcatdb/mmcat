package cz.matfyz.querying.exception;

import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.querying.parsing.Term;

import java.io.Serializable;
import java.util.List;

public class ProjectingException extends QueryingException {

    protected ProjectingException(String name, Serializable data) {
        super("projecting." + name, data, null);
    }

    public static ProjectingException notSingleComponent() {
        return new ProjectingException("notSingleComponent", null);
    }

    public static ProjectingException notRootVariable(Term term) {
        return new ProjectingException("notRootVariable", term.getIdentifier());
    }

    private record NotSingleRootData(
        List<SchemaObject> selectRoots
    ) implements Serializable {}

    public static ProjectingException notSingleRoot(List<SchemaObject> selectRoots) {
        return new ProjectingException("notSingleRoot", new NotSingleRootData(selectRoots));
    }

    private record PathNotFoundData(
        SchemaObject source,
        SchemaObject target
    ) implements Serializable {}

    public static ProjectingException pathNotFound(SchemaObject source, SchemaObject target) {
        return new ProjectingException("pathNotFound", new PathNotFoundData(source, target));
    }

}
