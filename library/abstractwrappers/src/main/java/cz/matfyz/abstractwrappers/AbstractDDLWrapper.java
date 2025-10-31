package cz.matfyz.abstractwrappers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
            final List<PathSegment> newSegments = new ArrayList<>(segments);
            newSegments.add(segment);
            return new PropertyPath(newSegments);
        }

        public PropertyPath addArray(int dimension) {
            final List<PathSegment> newSegments = new ArrayList<>(segments);
            final int lastIndex = newSegments.size() - 1;
            newSegments.set(lastIndex, PathSegment.array(newSegments.get(lastIndex), dimension));
            return new PropertyPath(newSegments);
        }

        @Override public String toString() {
            return segments.stream().map(PathSegment::toString).collect(Collectors.joining(PATH_SEPARATOR));
        }

    }

    public class PathSegment implements Serializable {

        /** Actually an immutable sorted set. */
        public final Set<String> names;
        public final boolean isMap;
        public final boolean isArray;

        /** If this is an array, it has this many dimensions. Minimum is 1. */
        private final int dimension;

        private PathSegment(Set<String> names, boolean isMap, int dimension) {
            this.names = names;
            this.isMap = isMap;
            this.isArray = dimension > 0;
            this.dimension = dimension;
        }

        public static PathSegment scalar(String name) {
            return new PathSegment(Set.of(name), false, 0);
        }

        public static PathSegment map(Set<String> names) {
            final var sortedNames = Collections.unmodifiableSortedSet(new TreeSet<>(names));
            return new PathSegment(sortedNames, true, 0);
        }

        public static PathSegment array(PathSegment segment, int dimension) {
            final var sum = (segment.isArray ? segment.dimension : 0) + dimension;
            return new PathSegment(segment.names, segment.isMap, sum);
        }

        public int arrayDimension() {
            assert isArray : "Can't get array dimension of non-array path segment.";
            return dimension;
        }

        @Override public String toString() {
            final var name = isMap
                ? "(" + names.stream().collect(Collectors.joining("|")) + ")"
                : names.iterator().next();

            return name + "[]".repeat(dimension);
        }

    }

}
