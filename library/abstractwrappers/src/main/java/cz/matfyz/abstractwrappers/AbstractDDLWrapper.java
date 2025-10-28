package cz.matfyz.abstractwrappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface AbstractDDLWrapper {

    String PATH_SEPARATOR = "/";
    String EMPTY_NAME = "";
    String INDENTATION = "    ";

    boolean isSchemaless();

    /**
     * Prepares the wrapper for the next kind. Very important - don't underestimate!
     */
    void clear();

    void setKindName(String name);

    /**
     * Throws an exception if the operation isn't supported.
     */
    void addProperty(PropertyPath path, boolean isComplex, boolean isRequired);

    AbstractStatement createDDLStatement();

    Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands);

    AbstractStatement createCreationStatement(String newDBName, String owner);

    /**
     * Immutable - all methods just create a new path.
     */
    public record PropertyPath(
        List<PathSegment> segments
    ) implements Serializable {

        public static PropertyPath empty() {
            return new PropertyPath(new ArrayList<>());
        }

        /**
         * Creates a new path with the added value.
         */
        public PropertyPath add(PathSegment segment) {
            List<PathSegment> newSegments = new ArrayList<>(segments);
            newSegments.add(segment);
            return new PropertyPath(newSegments);
        }

        @Override public String toString() {
            return segments.stream().map(PathSegment::toString).collect(Collectors.joining(PATH_SEPARATOR));
        }

    }

    public class PathSegment implements Serializable {

        /** Actually a set. */
        public final List<String> names;
        private final Type type;
        /** If this is an array, it has this many dimensions. Minimum is 1. */
        private final int dimension;

        private PathSegment(List<String> names, Type type, int dimension) {
            this.names = names;
            this.type = type;
            this.dimension = dimension;
        }

        public static PathSegment scalar(String name) {
            return new PathSegment(List.of(name), Type.SCALAR, 0);
        }

        public static PathSegment array(int dimension) {
            return new PathSegment(List.of(), Type.ARRAY, dimension);
        }

        public static PathSegment map(Set<String> names) {
            return new PathSegment(List.copyOf(names), Type.MAP, 0);
        }

        public enum Type {
            /** The names should contain exactly one value. */
            SCALAR,
            /** The names should be empty. */
            ARRAY,
            /** The names can contain any number of values. */
            MAP,
        }

        public boolean isArray() {
            return type == Type.ARRAY;
        }

        public int arrayDimension() {
            assert type == Type.ARRAY : "Can't get array dimension of non-array path segment.";
            return dimension;
        }

        @Override public String toString() {
            return switch (type) {
                case Type.SCALAR -> names.iterator().next();
                case Type.ARRAY -> names.iterator().next() + "[]".repeat(dimension);
                case Type.MAP -> "(" + names.stream().collect(Collectors.joining("|")) + ")";
            };
        }

    }

}
