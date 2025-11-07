package cz.matfyz.transformations.algorithms;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.mapping.AccessPath;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Name.DynamicName;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.SimpleProperty;
import cz.matfyz.core.mapping.Name.StringName;
import cz.matfyz.core.mapping.Name.TypedName;
import cz.matfyz.transformations.exception.InvalidStateException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @implNote A custom ordering of the elements of the arrays isn't supported in the current iteration of the framework.
 */
public class DMLAlgorithm {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(DMLAlgorithm.class);

    public static List<AbstractStatement> run(Mapping mapping, InstanceCategory instance, AbstractDMLWrapper wrapper) {
        return new DMLAlgorithm(mapping, instance, wrapper).run();
    }

    private final Mapping mapping;
    private final InstanceCategory instance;
    private final AbstractDMLWrapper wrapper;

    private DMLAlgorithm(Mapping mapping, InstanceCategory instance, AbstractDMLWrapper wrapper) {
        this.mapping = mapping;
        this.instance = instance;
        this.wrapper = wrapper;
    }

    private record StackJob(ComplexProperty complexProperty, String prefix, DomainRow row) {}

    private final Deque<StackJob> stack = new ArrayDeque<>();

    private List<AbstractStatement> run() {
        stack.clear();

        final List<AbstractStatement> output = new ArrayList<>();

        for (final DomainRow row : instance.getObjex(mapping.rootObjex()).allRows())
            output.add(buildStatement(row));

        return output;
    }

    private AbstractStatement buildStatement(DomainRow row) {
        wrapper.clear();
        wrapper.setKindName(mapping.kindName());

        stack.push(new StackJob(mapping.accessPath(), AbstractDDLWrapper.EMPTY_NAME, row));

        while (!stack.isEmpty()) {
            final StackJob job = stack.pop();
            processComplexProperty(job.complexProperty, job.prefix, job.row);
        }

        return wrapper.createDMLStatement();
    }

    private void processComplexProperty(ComplexProperty path, String prefix, DomainRow row) {
        for (final AccessPath subpath : path.subpaths()) {
            if (subpath.name() instanceof DynamicName) {
                processMap((ComplexProperty) subpath, prefix, row);
            }
            else {
                final String name = concatenatePaths(prefix, ((StringName) subpath.name()).value);
                processSubpath(subpath, name, row);
            }
        }
    }

    private void processMap(ComplexProperty map, String prefix, DomainRow parentRow) {
        final var keyProperty = map.getTypedSubpath(TypedName.KEY);
        final var valueSubpath = map.getTypedSubpath(TypedName.VALUE);

        for (final DomainRow entryRow : parentRow.traverseThrough(map.signature())) {
            final var suffix = entryRow.tryFindScalarValue(keyProperty.signature());
            if (suffix == null)
                throw InvalidStateException.dynamicNameNotFound((DynamicName) map.name());

            final String name = concatenatePaths(prefix, suffix);
            processSubpath(valueSubpath, name, entryRow);
        }
    }

    private void processSubpath(AccessPath subpath, String name, DomainRow row) {
        if (!(subpath instanceof ComplexProperty complex)) {
            processSimpleSubpath((SimpleProperty) subpath, name, row);
            return;
        }

        if (!complex.signature().hasDual()) {
            // Not an array.
            final var childRow = getScalarChildRow(complex, row);
            if (childRow == null)
                addEmpty(name);
            else
                addComplex(complex, name, childRow);

            return;
        }

        // It's an array. It still might have mapped indexes.
        if (!complex.getIndexSubpaths().isEmpty()) {
            processArray(complex, name, row);
            return;
        }

        // It's a complex array without mapped indexes.
        final var schemaPath = mapping.category().getPath(complex.signature());
        int index = 0;
        for (final DomainRow elementRow : row.traverseThrough(schemaPath)) {
            addComplex(complex, name + "[" + index + "]", elementRow);
            index++;
        }

        // If it's an array but there aren't any items in it, we return a simple pair with 'null' value.
        if (index == 0)
            addEmpty(name);
    }

    private @Nullable DomainRow getScalarChildRow(ComplexProperty path, DomainRow parentRow) {
        if (path.isAuxiliary())
            return parentRow;

        final var childRows = parentRow.traverseThrough(path.signature());
        assert childRows.size() <= 1 : "Complex property with a static name must have at most one value.";

        if (childRows.isEmpty())
            return null;

        return childRows.iterator().next();
    }

    private void processSimpleSubpath(SimpleProperty subpath, String name, DomainRow row) {
        // Now we know it's a normal property with a string name.
        if (!subpath.signature().hasDual()) {
            // Not an array.
            final @Nullable String value = row.tryFindScalarValue(subpath.signature());
            addSimple(name, value);
            return;
        }

        // It's a simple array without mapped indexes.
        int index = 0;
        for (final String value : row.findArrayValues(subpath.signature())) {
            addSimple(name + "[" + index + "]", value);
            index++;
        }

        // If it's an array but there aren't any items in it, we return a simple pair with 'null' value.
        if (index == 0)
            addEmpty(name);
    }

    private void processArray(ComplexProperty array, String prefix, DomainRow parentRow) {
        final var indexSignatures = array.getIndexSubpaths().stream().map(AccessPath::signature).toArray(Signature[]::new);
        final var schemaPath = mapping.category().getPath(array.signature());

        // First, we sort all elements by their indexes.
        final var elements = parentRow.traverseThrough(schemaPath).stream()
            .map(elementRow -> {
                final int[] indexes = new int[indexSignatures.length];
                for (int i = 0; i < indexSignatures.length; i++) {
                    final var indexValue = elementRow.tryFindScalarValue(indexSignatures[i]);
                    if (indexValue == null)
                        throw InvalidStateException.indexNotFound(indexSignatures[i]);

                    indexes[i] = Integer.parseInt(indexValue);
                }

                return new ArrayElement(indexes, elementRow);
            })
            .sorted().toList();

        final var valueSubpath = array.getTypedSubpath(TypedName.VALUE);

        // Then we add them one by one.
        for (final var element : elements) {
            final var name = element.stringifyIndexes(prefix);
            processSubpath(valueSubpath, name, element.row);
        }
    }

    private record ArrayElement(int[] indexes, DomainRow row) implements Comparable<ArrayElement> {

        @Override public int compareTo(ArrayElement other) {
            for (int i = 0; i < indexes.length; i++) {
                final var indexComparison = this.indexes[i] - other.indexes[i];
                if (indexComparison != 0)
                    return indexComparison;
            }

            return 0;
        }

        public String stringifyIndexes(String name) {
            final StringBuilder sb = new StringBuilder(name);
            for (final int index : indexes)
                sb.append("[").append(index).append("]");

            return sb.toString();
        }

    }

    private void addEmpty(String name) {
        wrapper.append(name, null);
    }

    private void addSimple(String name, @Nullable String simpleValue) {
        wrapper.append(name, simpleValue);
    }

    private void addComplex(ComplexProperty subpath, String name, DomainRow complexValue) {
        stack.push(new StackJob(subpath, name, complexValue));
    }

    private static String concatenatePaths(String path1, String path2) {
        return path1 + (path1.equals(AbstractDDLWrapper.EMPTY_NAME) ? "" : AbstractDDLWrapper.PATH_SEPARATOR) + path2;
    }

}
