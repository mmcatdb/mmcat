package cz.matfyz.abstractwrappers;

import cz.matfyz.core.mapping.StaticName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface AbstractDDLWrapper {

    String PATH_SEPARATOR = "/";
    String EMPTY_NAME = StaticName.createAnonymous().getStringName();
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

    public record PathSegment(
        Set<String> names,
        /** If false, the names should contain exactly one element. */
        boolean isDynamic,
        /** An array property contains an array of values. */
        boolean isArray
    ) implements Serializable {
        @Override public String toString() {
            final String options = isDynamic
                ? "(" + names.stream().collect(Collectors.joining("|")) + ")"
                : names.iterator().next();

            return options + (isArray ? "[]" : "");
        }
    }

}
